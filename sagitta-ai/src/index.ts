/**
 * Sagitta AI Worker — the Blaze-free twin of backend/functions.
 *
 * Same request/response contracts as the Firebase callables, but as plain
 * HTTPS so the AI coach runs without Firebase Blaze (no payment method):
 *   POST /proxyAI        {model?, messages, max_tokens?|maxTokens?, temperature?}
 *                     -> {id, content, finishReason, usage{promptTokens, completionTokens, totalTokens}}
 *   POST /streamAI       same body -> OpenRouter SSE passthrough (deltas, usage, [DONE])
 *   POST /deepResearchFn {question, depth?} -> {question, answer, sources, followUps, usedLiveSearch}
 *   POST /leetcodeStats  {username} -> {totalSolved, easySolved, mediumSolved, hardSolved, ranking, cached}
 *   GET  /health         -> {status, timestamp, version}
 *
 * Auth on every endpoint: `Authorization: Bearer <Firebase ID token>`,
 * RS256-verified against Google's public JWKs for FIREBASE_PROJECT_ID. The
 * upstream OpenRouter key lives in a Worker secret and never reaches a client.
 *
 * Quota: enforced client-side (AIRateLimiter, 10 answers/day) and bounded by
 * the OpenRouter free-tier account cap (50 free-model requests/day). The
 * Firebase path (backend/functions) keeps the stronger Firestore quota for a
 * future Blaze deployment — this Worker is its port, not its replacement.
 */

export interface Env {
  OPENROUTER_API_KEY: string;
  FIREBASE_PROJECT_ID: string;
}

const OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
const LEETCODE_GRAPHQL_URL = "https://leetcode.com/graphql";
const JWK_URL =
  "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";
const VERSION = "2.1.0";

/**
 * Free model verified live on 2026-11-08 (gemma-4-31b:free was upstream-429).
 * The allowlist stays single-model so a client can never steer cost upward —
 * same policy as proxy.ts, on a model the $0-balance account can actually use.
 */
const DEFAULT_MODEL = "nvidia/nemotron-3-ultra-550b-a55b:free";
const ALLOWED_MODELS = new Set([DEFAULT_MODEL]);

const MAX_TOKENS_PER_REQUEST = 1024;
const MAX_MESSAGES = 30;
const MAX_MESSAGE_CHARS = 12_000;
const MAX_TOTAL_PROMPT_CHARS = 48_000;
const PROXY_UPSTREAM_TIMEOUT_MS = 25_000;
const STREAM_UPSTREAM_TIMEOUT_MS = 90_000;
const RESEARCH_PROMPT_CHAR_LIMIT = 1_200;
const LEETCODE_UPSTREAM_TIMEOUT_MS = 8_000;
const LEETCODE_CACHE_TTL_S = 600;
const USERNAME_PATTERN = /^[A-Za-z0-9_-]{1,40}$/;
const ALLOWED_ROLES = new Set(["system", "user", "assistant"]);

type ErrorStatus =
  | "UNAUTHENTICATED"
  | "INVALID_ARGUMENT"
  | "NOT_FOUND"
  | "RESOURCE_EXHAUSTED"
  | "FAILED_PRECONDITION"
  | "UNAVAILABLE"
  | "METHOD_NOT_ALLOWED";

interface ChatMessage {
  role: string;
  content: string;
}

interface AIRequest {
  model?: string;
  messages: ChatMessage[];
  max_tokens?: number;
  /** Legacy Android field name; accepted so older clients keep their budget. */
  maxTokens?: number;
  temperature?: number;
}

interface ResearchRequest {
  question?: string;
  depth?: string;
}

// ---------------------------------------------------------------------------
// errors
// ---------------------------------------------------------------------------

function errorResponse(
  status: number,
  errorStatus: ErrorStatus,
  message: string
): Response {
  return new Response(
    JSON.stringify({ error: { status: errorStatus, message } }),
    {
      status,
      headers: { "Content-Type": "application/json; charset=utf-8" },
    }
  );
}

function ok(data: unknown): Response {
  return new Response(JSON.stringify(data), {
    status: 200,
    headers: { "Content-Type": "application/json; charset=utf-8" },
  });
}

// ---------------------------------------------------------------------------
// Firebase ID token verification (RS256, Google public JWKs)
// ---------------------------------------------------------------------------

let jwkCache: { keys: Map<string, JsonWebKey>; fetchedAt: number } | null =
  null;
const JWK_CACHE_MS = 60 * 60 * 1000;

async function getJwk(kid: string): Promise<JsonWebKey | null> {
  const load = async (): Promise<Map<string, JsonWebKey>> => {
    const res = await fetch(JWK_URL);
    if (!res.ok) throw new Error(`JWK fetch failed: ${res.status}`);
    const body = (await res.json()) as { keys: Array<Record<string, string>> };
    const keys = new Map<string, JsonWebKey>();
    for (const k of body.keys) {
      if (k.kid && k.kty === "RSA") keys.set(k.kid, k as unknown as JsonWebKey);
    }
    return keys;
  };

  if (!jwkCache || Date.now() - jwkCache.fetchedAt > JWK_CACHE_MS) {
    jwkCache = { keys: await load(), fetchedAt: Date.now() };
  }
  const hit = jwkCache.keys.get(kid);
  if (hit) return hit;
  // Rotation: an unknown kid is worth one refresh before rejecting.
  jwkCache = { keys: await load(), fetchedAt: Date.now() };
  return jwkCache.keys.get(kid) ?? null;
}

function bearerToken(req: Request): string | null {
  const header = req.headers.get("authorization");
  if (!header) return null;
  const match = /^Bearer\s+(.+)$/i.exec(header.trim());
  return match ? match[1].trim() : null;
}

/**
 * Verifies signature, expiry, audience, issuer and subject. Returns the uid.
 * Throws on any failure — callers map that to 401.
 */
async function verifyIdToken(token: string, projectId: string): Promise<string> {
  const parts = token.split(".");
  if (parts.length !== 3) throw new Error("malformed token");
  const [headerB64, payloadB64, signatureB64] = parts;

  const header = JSON.parse(atobUrlSafe(headerB64)) as { kid?: string; alg?: string };
  if (header.alg !== "RS256" || !header.kid) throw new Error("unexpected header");
  const jwk = await getJwk(header.kid);
  if (!jwk) throw new Error("unknown kid");

  const key = await crypto.subtle.importKey(
    "jwk",
    jwk,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["verify"]
  );
  const data = new TextEncoder().encode(`${headerB64}.${payloadB64}`);
  const signature = base64UrlDecode(signatureB64);
  const valid = await crypto.subtle.verify(
    "RSASSA-PKCS1-v1_5",
    key,
    signature,
    data
  );
  if (!valid) throw new Error("bad signature");

  const claims = JSON.parse(atobUrlSafe(payloadB64)) as {
    exp?: number;
    aud?: string;
    iss?: string;
    sub?: string;
  };
  const now = Math.floor(Date.now() / 1000);
  if (!claims.exp || claims.exp < now) throw new Error("expired");
  if (claims.aud !== projectId) throw new Error("wrong audience");
  if (claims.iss !== `https://securetoken.google.com/${projectId}`)
    throw new Error("wrong issuer");
  if (!claims.sub) throw new Error("missing subject");
  return claims.sub;
}

function atobUrlSafe(b64: string): string {
  return atob(b64.replace(/-/g, "+").replace(/_/g, "/"));
}

function base64UrlDecode(b64: string): Uint8Array {
  const normalized = atobUrlSafe(b64);
  const bytes = new Uint8Array(normalized.length);
  for (let i = 0; i < normalized.length; i++) bytes[i] = normalized.charCodeAt(i);
  return bytes;
}

// ---------------------------------------------------------------------------
// request validation — port of proxy.ts validateRequest
// ---------------------------------------------------------------------------

export function validateRequest(data: AIRequest): AIRequest {
  if (!data || typeof data !== "object") {
    throw new Error("Invalid request body.");
  }
  if (!Array.isArray(data.messages) || data.messages.length === 0) {
    throw new Error("messages must be a non-empty array.");
  }
  if (data.messages.length > MAX_MESSAGES) {
    throw new Error(`Too many messages (max ${MAX_MESSAGES}).`);
  }

  const model = data.model || DEFAULT_MODEL;
  if (!ALLOWED_MODELS.has(model)) {
    throw new Error("Model not allowed.");
  }

  let totalChars = 0;
  let systemMessages = 0;
  const messages = data.messages.map((m, index) => {
    if (!m || typeof m !== "object") {
      throw new Error("Invalid message.");
    }
    const role = typeof m.role === "string" ? m.role : "user";
    if (!ALLOWED_ROLES.has(role)) {
      throw new Error("Message role not allowed.");
    }
    if (role === "system") {
      systemMessages += 1;
      if (index !== 0 || systemMessages > 1) {
        throw new Error("A single system message is only allowed first.");
      }
    }
    const content = typeof m.content === "string" ? m.content : "";
    if (content.length > MAX_MESSAGE_CHARS) {
      throw new Error(`Message exceeds ${MAX_MESSAGE_CHARS} characters.`);
    }
    totalChars += content.length;
    return { role, content };
  });
  if (totalChars > MAX_TOTAL_PROMPT_CHARS) {
    throw new Error(`Prompt exceeds ${MAX_TOTAL_PROMPT_CHARS} characters.`);
  }

  // Older Android builds serialise the Kotlin field name (maxTokens) rather
  // than the snake_case the API documents; accept both so a token budget
  // silently resetting to the server default is impossible.
  const requestedTokens = Number(data.max_tokens ?? data.maxTokens);
  const requestedTemperature = Number(data.temperature);
  return {
    model,
    messages,
    max_tokens: Math.min(
      Math.max(Number.isFinite(requestedTokens) ? requestedTokens : 512, 1),
      MAX_TOKENS_PER_REQUEST
    ),
    temperature: Math.min(
      Math.max(Number.isFinite(requestedTemperature) ? requestedTemperature : 0.7, 0),
      2
    ),
  };
}

async function readJson(req: Request): Promise<Record<string, unknown>> {
  try {
    const body = await req.json();
    if (!body || typeof body !== "object") throw new Error("not an object");
    return body as Record<string, unknown>;
  } catch {
    throw new Error("Invalid JSON body.");
  }
}

async function requireAuth(
  req: Request,
  env: Env
): Promise<string | Response> {
  const token = bearerToken(req);
  if (!token) {
    return errorResponse(401, "UNAUTHENTICATED", "Sign in required.");
  }
  try {
    return await verifyIdToken(token, env.FIREBASE_PROJECT_ID);
  } catch {
    return errorResponse(401, "UNAUTHENTICATED", "Invalid or expired token.");
  }
}

function apiKeyOr503(env: Env): string | Response {
  const key = env.OPENROUTER_API_KEY;
  if (!key) {
    return errorResponse(503, "FAILED_PRECONDITION", "AI service is not configured.");
  }
  return key;
}

interface AIResponse {
  id: string;
  choices: Array<{ message: { content: string }; finish_reason: string }>;
  usage?: { prompt_tokens: number; completion_tokens: number; total_tokens: number };
}

async function callOpenRouter(
  apiKey: string,
  body: Record<string, unknown>,
  timeoutMs: number
): Promise<Response> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  try {
    return await fetch(OPENROUTER_URL, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${apiKey}`,
        "Content-Type": "application/json",
        "HTTP-Referer": "https://nexusapp.in",
        "X-Title": "Sagitta",
      },
      body: JSON.stringify({
        // The default model is a reasoning model: its hidden reasoning would
        // eat the token budget and delay/dilute the visible answer, so it is
        // excluded at the OpenRouter level for every call.
        reasoning: { exclude: true },
        ...body,
      }),
      signal: controller.signal,
    });
  } finally {
    clearTimeout(timer);
  }
}

// ---------------------------------------------------------------------------
// /proxyAI
// ---------------------------------------------------------------------------

async function handleProxyAI(req: Request, env: Env): Promise<Response> {
  const auth = await requireAuth(req, env);
  if (auth instanceof Response) return auth;

  let requestBody: AIRequest;
  try {
    requestBody = validateRequest((await readJson(req)) as unknown as AIRequest);
  } catch (e) {
    return errorResponse(400, "INVALID_ARGUMENT", (e as Error).message);
  }

  const apiKey = apiKeyOr503(env);
  if (apiKey instanceof Response) return apiKey;

  let upstream: Response;
  try {
    upstream = await callOpenRouter(apiKey, requestBody, PROXY_UPSTREAM_TIMEOUT_MS);
  } catch {
    return errorResponse(502, "UNAVAILABLE", "AI request failed. Please try again.");
  }

  if (!upstream.ok) {
    // Upstream error bodies stay server-side; clients get a generic message.
    const detail = await upstream.text().catch(() => "");
    console.error(`proxyAI upstream ${upstream.status}: ${detail.slice(0, 500)}`);
    return errorResponse(502, "UNAVAILABLE", "AI service is temporarily unavailable.");
  }

  const result = (await upstream.json()) as AIResponse;
  const totalTokens = Number(result.usage?.total_tokens || 0);
  return ok({
    id: result.id,
    content: result.choices[0]?.message?.content ?? "",
    finishReason: result.choices[0]?.finish_reason ?? "stop",
    usage: {
      promptTokens: result.usage?.prompt_tokens ?? 0,
      completionTokens: result.usage?.completion_tokens ?? 0,
      totalTokens,
    },
  });
}

// ---------------------------------------------------------------------------
// /streamAI — SSE passthrough (port of stream.ts)
// ---------------------------------------------------------------------------

async function handleStreamAI(req: Request, env: Env): Promise<Response> {
  const auth = await requireAuth(req, env);
  if (auth instanceof Response) return auth;

  let requestBody: AIRequest;
  try {
    requestBody = validateRequest((await readJson(req)) as unknown as AIRequest);
  } catch (e) {
    return errorResponse(400, "INVALID_ARGUMENT", (e as Error).message);
  }

  const apiKey = apiKeyOr503(env);
  if (apiKey instanceof Response) return apiKey;

  let upstream: Response;
  try {
    upstream = await callOpenRouter(
      apiKey,
      { ...requestBody, stream: true, stream_options: { include_usage: true } },
      STREAM_UPSTREAM_TIMEOUT_MS
    );
  } catch {
    return errorResponse(502, "UNAVAILABLE", "AI service is temporarily unavailable.");
  }

  if (!upstream.ok || !upstream.body) {
    const detail = await upstream.text().catch(() => "");
    console.error(`streamAI upstream ${upstream.status}: ${detail.slice(0, 500)}`);
    return errorResponse(502, "UNAVAILABLE", "AI service is temporarily unavailable.");
  }

  // Straight passthrough: the client parses deltas, usage and [DONE] from the
  // OpenRouter wire format, so no re-framing here.
  return new Response(upstream.body, {
    status: 200,
    headers: {
      "Content-Type": "text/event-stream; charset=utf-8",
      "Cache-Control": "no-cache, no-transform",
      "X-Accel-Buffering": "no",
    },
  });
}

// ---------------------------------------------------------------------------
// /deepResearchFn — port of research.ts (no Firestore logging)
// ---------------------------------------------------------------------------

interface ResearchSource {
  title: string;
  url?: string;
  snippet?: string;
}

function clampDepth(depth?: string): "quick" | "standard" | "deep" {
  return depth === "quick" || depth === "deep" ? depth : "standard";
}

function followUpCount(depth: string): number {
  return depth === "quick" ? 2 : depth === "deep" ? 6 : 4;
}

function parseJsonBlock<T>(text: string, fallback: T): T {
  // Reasoning models sometimes wrap JSON in markdown fences; strip them and
  // take the outermost brace block before parsing.
  const stripped = text.replace(/```(?:json)?/gi, "");
  const match = stripped.match(/\{[\s\S]*\}/);
  if (!match) return fallback;
  try {
    return JSON.parse(match[0]) as T;
  } catch {
    return fallback;
  }
}

async function callModel(
  apiKey: string,
  messages: ChatMessage[],
  maxTokens: number
): Promise<string> {
  const upstream = await fetch(OPENROUTER_URL, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${apiKey}`,
      "Content-Type": "application/json",
      "HTTP-Referer": "https://nexusapp.in",
      "X-Title": "Sagitta",
    },
    body: JSON.stringify({
      model: DEFAULT_MODEL,
      messages,
      // Full clamp headroom: reasoning models spend budget before the visible
      // answer, so a smaller cap risks truncating the JSON mid-object.
      max_tokens: 1024,
      temperature: 0.4,
      reasoning: { exclude: true },
    }),
  });
  if (!upstream.ok) {
    console.error(
      `research model error ${upstream.status}: ${(await upstream.text().catch(() => "")).slice(0, 400)}`
    );
    throw new Error("Research model unavailable.");
  }
  const data = (await upstream.json()) as AIResponse;
  return data.choices?.[0]?.message?.content ?? "";
}

async function handleDeepResearch(req: Request, env: Env): Promise<Response> {
  const auth = await requireAuth(req, env);
  if (auth instanceof Response) return auth;

  let body: ResearchRequest;
  try {
    body = (await readJson(req)) as unknown as ResearchRequest;
  } catch (e) {
    return errorResponse(400, "INVALID_ARGUMENT", (e as Error).message);
  }

  const question = (body.question || "").trim();
  if (question.length < 8 || question.length > RESEARCH_PROMPT_CHAR_LIMIT) {
    return errorResponse(
      400,
      "INVALID_ARGUMENT",
      "Question must be 8-1200 characters."
    );
  }
  const apiKey = apiKeyOr503(env);
  if (apiKey instanceof Response) return apiKey;
  const depth = clampDepth(body.depth);

  const synthesis = await callModel(
    apiKey,
    [
      {
        role: "system",
        content:
          "You are Sagitta Research, a career-intelligence assistant for engineering students in India. " +
          "Answer the user's question with concrete, current, India-relevant guidance. " +
          "Respond ONLY with JSON of shape: " +
          '{"answer": string (markdown, <=250 words), "sources": [{"title": string, "url"?: string, "snippet"?: string}], "usedLiveSearch": boolean}. ' +
          "List 2-4 sources you rely on; if any source is from your training knowledge rather than a " +
          "live lookup, set usedLiveSearch=false and omit fabricated URLs.",
      },
      { role: "user", content: question },
    ],
    depth === "deep" ? 1024 : 700
  );

  const parsed = parseJsonBlock<{
    answer?: string;
    sources?: ResearchSource[];
    usedLiveSearch?: boolean;
  }>(synthesis, {});

  const followUps = await callModel(
    apiKey,
    [
      {
        role: "system",
        content:
          `Generate exactly ${followUpCount(depth)} short follow-up questions (each <=90 chars) that a ` +
          "student would naturally ask next to go deeper on the topic. " +
          'Respond ONLY as JSON: {"followUps": string[]}.',
      },
      {
        role: "user",
        content: question + "\n\nAnswer summary: " + (parsed.answer || "").slice(0, 800),
      },
    ],
    300
  );
  const parsedFollowUps = parseJsonBlock<{ followUps?: string[] }>(followUps, {});

  return ok({
    question,
    answer: parsed.answer || "I couldn't complete this research right now. Try rephrasing.",
    sources: (parsed.sources || []).slice(0, 6),
    followUps: (parsedFollowUps.followUps || []).slice(0, followUpCount(depth)),
    usedLiveSearch: parsed.usedLiveSearch === true,
  });
}

// ---------------------------------------------------------------------------
// /leetcodeStats — port of stats.ts, Cache API instead of Firestore
// ---------------------------------------------------------------------------

interface LeetCodeStats {
  totalSolved: number;
  easySolved: number;
  mediumSolved: number;
  hardSolved: number;
  ranking: number;
}

const STATS_QUERY = `
  query userStats($username: String!) {
    matchedUser(username: $username) {
      profile { ranking }
      submitStats {
        acSubmissionNum { difficulty count }
      }
    }
  }
`;

function normalizeUsername(raw: unknown): string {
  if (typeof raw !== "string" || !USERNAME_PATTERN.test(raw.trim())) {
    throw new Error(
      "Usernames may contain letters, numbers, underscores and hyphens (max 40)."
    );
  }
  return raw.trim();
}

function mapStats(payload: unknown, username: string): LeetCodeStats {
  const data = (payload as {
    data?: {
      matchedUser?: {
        profile?: { ranking?: number };
        submitStats?: {
          acSubmissionNum?: Array<{ difficulty?: string; count?: number }>;
        };
      } | null;
    };
  }).data;
  const user = data?.matchedUser;
  if (!user) {
    throw new Error(`No public LeetCode profile found for "${username}".`);
  }
  const counts = user.submitStats?.acSubmissionNum ?? [];
  const countOf = (difficulty: string): number =>
    counts.find((c) => c.difficulty?.toLowerCase() === difficulty.toLowerCase())?.count ?? 0;
  return {
    totalSolved: countOf("All"),
    easySolved: countOf("Easy"),
    mediumSolved: countOf("Medium"),
    hardSolved: countOf("Hard"),
    ranking: user.profile?.ranking ?? 0,
  };
}

async function handleLeetCodeStats(
  req: Request,
  env: Env,
  ctx: ExecutionContext
): Promise<Response> {
  const auth = await requireAuth(req, env);
  if (auth instanceof Response) return auth;

  let username: string;
  try {
    const body = await readJson(req);
    username = normalizeUsername(body.username);
  } catch (e) {
    return errorResponse(400, "INVALID_ARGUMENT", (e as Error).message);
  }

  // Cache API only stores GETs, so the POST result is cached under a
  // synthetic GET key — same 10-minute TTL the Firestore cache used.
  const cacheKey = new Request(`https://sagitta-ai.cache/leetcode/${username}`, {
    method: "GET",
  });
  const cached = await caches.default.match(cacheKey);
  if (cached) {
    const stats = (await cached.json()) as LeetCodeStats;
    return ok({ ...stats, cached: true });
  }

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), LEETCODE_UPSTREAM_TIMEOUT_MS);
  let upstream: Response;
  try {
    upstream = await fetch(LEETCODE_GRAPHQL_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        query: STATS_QUERY,
        variables: { username },
      }),
      signal: controller.signal,
    });
  } catch {
    clearTimeout(timer);
    return errorResponse(502, "UNAVAILABLE", "LeetCode is temporarily unavailable.");
  } finally {
    clearTimeout(timer);
  }

  if (!upstream.ok) {
    return errorResponse(502, "UNAVAILABLE", "LeetCode is temporarily unavailable.");
  }

  let stats: LeetCodeStats;
  try {
    stats = mapStats(await upstream.json(), username);
  } catch (e) {
    return errorResponse(404, "NOT_FOUND", (e as Error).message);
  }

  const payload = { ...stats, cached: false };
  const cacheable = new Response(JSON.stringify(stats), {
    headers: { "Cache-Control": `public, max-age=${LEETCODE_CACHE_TTL_S}` },
  });
  ctx.waitUntil(caches.default.put(cacheKey, cacheable));
  return ok(payload);
}

// ---------------------------------------------------------------------------
// /health
// ---------------------------------------------------------------------------

function handleHealth(): Response {
  return ok({ status: "ok", timestamp: Date.now(), version: VERSION });
}

// ---------------------------------------------------------------------------
// router
// ---------------------------------------------------------------------------

export default {
  async fetch(
    request: Request,
    env: Env,
    ctx: ExecutionContext
  ): Promise<Response> {
    const { pathname } = new URL(request.url);
    const route = pathname.replace(/\/+$/, "") || "/";

    try {
      if (route === "/health" && request.method === "GET") {
        return handleHealth();
      }
      if (request.method !== "POST") {
        return errorResponse(405, "METHOD_NOT_ALLOWED", "Use POST.");
      }
      switch (route) {
        case "/proxyAI":
          return await handleProxyAI(request, env);
        case "/streamAI":
          return await handleStreamAI(request, env);
        case "/deepResearchFn":
          return await handleDeepResearch(request, env);
        case "/leetcodeStats":
          return await handleLeetCodeStats(request, env, ctx);
        default:
          return errorResponse(404, "NOT_FOUND", "Unknown endpoint.");
      }
    } catch (e) {
      console.error(
        `unhandled ${route}:`,
        e instanceof Error ? e.message : String(e)
      );
      return errorResponse(502, "UNAVAILABLE", "AI request failed. Please try again.");
    }
  },
};
