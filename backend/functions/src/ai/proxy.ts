import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { FieldValue } from "firebase-admin/firestore";
import { requireAuth } from "../middleware/auth";

/**
 * Hardened AI proxy.
 *
 * Controls:
 * - Model allowlist: clients cannot pick arbitrary (expensive) models.
 * - max_tokens clamp: bounded completion cost per request.
 * - Input size caps: bounded prompt cost per request.
 * - Per-user daily quota (requests + tokens) enforced against ai_proxy_logs.
 * - Upstream error bodies are logged server-side, never returned to clients.
 */

const ALLOWED_MODELS = new Set([
  "openai/gpt-4o-mini",
]);

const DEFAULT_MODEL = "openai/gpt-4o-mini";
const MAX_TOKENS_PER_REQUEST = 1024;
const MAX_MESSAGES = 30;
const MAX_MESSAGE_CHARS = 12_000;
const MAX_TOTAL_PROMPT_CHARS = 48_000;

const DAILY_REQUEST_LIMIT = 40;
const DAILY_TOKEN_LIMIT = 60_000;
const UPSTREAM_TIMEOUT_MS = 25_000;
const ALLOWED_ROLES = new Set(["system", "user", "assistant"]);

const OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

export interface AIRequest {
  model?: string;
  messages: Array<{ role: string; content: string }>;
  max_tokens?: number;
  temperature?: number;
}

export const PROMPT_LIMITS = {
  maxTokensPerRequest: MAX_TOKENS_PER_REQUEST,
  maxMessages: MAX_MESSAGES,
  maxMessageChars: MAX_MESSAGE_CHARS,
  maxTotalPromptChars: MAX_TOTAL_PROMPT_CHARS,
  dailyRequestLimit: DAILY_REQUEST_LIMIT,
  dailyTokenLimit: DAILY_TOKEN_LIMIT,
} as const;

interface AIResponse {
  id: string;
  choices: Array<{
    message: { role: string; content: string };
    finish_reason: string;
  }>;
  usage?: {
    prompt_tokens: number;
    completion_tokens: number;
    total_tokens: number;
  };
}

function getApiKey(): string {
  const key = process.env.OPENROUTER_API_KEY;
  if (!key) {
    throw new functions.https.HttpsError(
      "failed-precondition",
      "AI service is not configured."
    );
  }
  return key;
}

export function validateRequest(data: AIRequest): AIRequest {
  if (!data || typeof data !== "object") {
    throw new functions.https.HttpsError("invalid-argument", "Invalid request body.");
  }
  if (!Array.isArray(data.messages) || data.messages.length === 0) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "messages must be a non-empty array."
    );
  }
  if (data.messages.length > MAX_MESSAGES) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      `Too many messages (max ${MAX_MESSAGES}).`
    );
  }

  const model = data.model || DEFAULT_MODEL;
  if (!ALLOWED_MODELS.has(model)) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Model not allowed."
    );
  }

  let totalChars = 0;
  let systemMessages = 0;
  const messages = data.messages.map((m, index) => {
    if (!m || typeof m !== "object") {
      throw new functions.https.HttpsError("invalid-argument", "Invalid message.");
    }
    const role = typeof m.role === "string" ? m.role : "user";
    if (!ALLOWED_ROLES.has(role)) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Message role not allowed."
      );
    }
    if (role === "system") {
      systemMessages += 1;
      if (index !== 0 || systemMessages > 1) {
        throw new functions.https.HttpsError(
          "invalid-argument",
          "A single system message is only allowed first."
        );
      }
    }
    const content = typeof m.content === "string" ? m.content : "";
    if (content.length > MAX_MESSAGE_CHARS) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        `Message exceeds ${MAX_MESSAGE_CHARS} characters.`
      );
    }
    totalChars += content.length;
    return { role, content };
  });
  if (totalChars > MAX_TOTAL_PROMPT_CHARS) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      `Prompt exceeds ${MAX_TOTAL_PROMPT_CHARS} characters.`
    );
  }

  const requestedTokens = Number(data.max_tokens);
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

export function usageDocId(uid: string, now: Date): string {
  const day = now.toISOString().slice(0, 10);
  return `${uid}_${day}`;
}

/**
 * Reserves one request in a transaction before the upstream call. A
 * read-then-call check allowed N parallel requests to all pass the quota.
 */
async function reserveQuota(uid: string): Promise<string> {
  const db = admin.firestore();
  const docId = usageDocId(uid, new Date());
  const ref = db.collection("ai_usage").doc(docId);

  await db.runTransaction(async (tx) => {
    const snap = await tx.get(ref);
    const requestCount = Number(snap.get("requestCount") || 0);
    const tokenCount = Number(snap.get("tokenCount") || 0);

    if (requestCount >= DAILY_REQUEST_LIMIT) {
      throw new functions.https.HttpsError(
        "resource-exhausted",
        "Daily AI request limit reached. Try again tomorrow."
      );
    }
    if (tokenCount >= DAILY_TOKEN_LIMIT) {
      throw new functions.https.HttpsError(
        "resource-exhausted",
        "Daily AI token limit reached. Try again tomorrow."
      );
    }

    tx.set(
      ref,
      {
        uid,
        day: docId.slice(docId.lastIndexOf("_") + 1),
        requestCount: requestCount + 1,
        tokenCount,
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  });

  return docId;
}

async function recordTokens(docId: string, totalTokens: number): Promise<void> {
  if (totalTokens <= 0) return;
  await admin
    .firestore()
    .collection("ai_usage")
    .doc(docId)
    .set(
      {
        tokenCount: FieldValue.increment(totalTokens),
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
}

export const aiProxy: functions.HttpsFunction = functions.https.onCall(
  async (data: AIRequest, context: functions.https.CallableContext) => {
    const uid = await requireAuth(context);
    const requestBody = validateRequest(data);
    const usageDocId = await reserveQuota(uid);

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), UPSTREAM_TIMEOUT_MS);

    try {
      const response = await fetch(OPENROUTER_URL, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${getApiKey()}`,
          "Content-Type": "application/json",
          "HTTP-Referer": "https://nexusapp.in",
          "X-Title": "NEXUS",
        },
        body: JSON.stringify(requestBody),
        signal: controller.signal,
      });

      if (!response.ok) {
        const errorBody = await response.text();
        console.error(
          `aiProxy upstream error uid=${uid} status=${response.status} body=${errorBody.slice(0, 500)}`
        );
        throw new functions.https.HttpsError(
          "internal",
          "AI service is temporarily unavailable."
        );
      }

      const result = (await response.json()) as AIResponse;
      const totalTokens = Number(result.usage?.total_tokens || 0);
      await recordTokens(usageDocId, totalTokens);

      await admin.firestore().collection("ai_proxy_logs").add({
        uid,
        model: requestBody.model,
        promptTokens: result.usage?.prompt_tokens ?? 0,
        completionTokens: result.usage?.completion_tokens ?? 0,
        totalTokens,
        outcome: "success",
        timestamp: FieldValue.serverTimestamp(),
      });

      return {
        id: result.id,
        content: result.choices[0]?.message?.content ?? "",
        finishReason: result.choices[0]?.finish_reason ?? "stop",
        usage: {
          promptTokens: result.usage?.prompt_tokens ?? 0,
          completionTokens: result.usage?.completion_tokens ?? 0,
          totalTokens,
        },
      };
    } catch (error) {
      if (error instanceof functions.https.HttpsError) throw error;
      const aborted = controller.signal.aborted;
      console.error(
        `aiProxy failed uid=${uid} aborted=${aborted}:`,
        error instanceof Error ? error.message : String(error)
      );
      await admin
        .firestore()
        .collection("ai_proxy_logs")
        .add({
          uid,
          model: requestBody.model,
          outcome: aborted ? "timeout" : "error",
          timestamp: FieldValue.serverTimestamp(),
        })
        .catch(() => undefined);
      throw new functions.https.HttpsError(
        aborted ? "deadline-exceeded" : "internal",
        aborted ? "AI request timed out. Please try again." : "AI request failed. Please try again."
      );
    } finally {
      clearTimeout(timeout);
    }
  }
);
