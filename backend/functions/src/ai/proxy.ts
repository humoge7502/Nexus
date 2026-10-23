import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
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

const OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

interface AIRequest {
  model?: string;
  messages: Array<{ role: string; content: string }>;
  max_tokens?: number;
  temperature?: number;
}

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

function validateRequest(data: AIRequest): AIRequest {
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
  const messages = data.messages.map((m) => {
    const role = typeof m.role === "string" ? m.role : "user";
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

  return {
    model,
    messages,
    max_tokens: Math.min(Math.max(data.max_tokens || 512, 1), MAX_TOKENS_PER_REQUEST),
    temperature: Math.min(Math.max(data.temperature ?? 0.7, 0), 2),
  };
}

async function enforceQuota(uid: string): Promise<void> {
  const dayStart = new Date();
  dayStart.setUTCHours(0, 0, 0, 0);

  const usageSnap = await admin
    .firestore()
    .collection("ai_proxy_logs")
    .where("uid", "==", uid)
    .where("timestamp", ">=", admin.firestore.Timestamp.fromDate(dayStart))
    .get();

  let requestCount = 0;
  let tokenCount = 0;
  usageSnap.forEach((doc) => {
    requestCount += 1;
    tokenCount += Number(doc.get("totalTokens") || 0);
  });

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
}

export const aiProxy: functions.HttpsFunction = functions.https.onCall(
  async (data: AIRequest, context: functions.https.CallableContext) => {
    const uid = await requireAuth(context);
    const requestBody = validateRequest(data);
    await enforceQuota(uid);

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

      await admin.firestore().collection("ai_proxy_logs").add({
        uid,
        model: requestBody.model,
        promptTokens: result.usage?.prompt_tokens ?? 0,
        completionTokens: result.usage?.completion_tokens ?? 0,
        totalTokens: result.usage?.total_tokens ?? 0,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
      });

      return {
        id: result.id,
        content: result.choices[0]?.message?.content ?? "",
        finishReason: result.choices[0]?.finish_reason ?? "stop",
        usage: result.usage,
      };
    } catch (error) {
      if (error instanceof functions.https.HttpsError) throw error;
      console.error(`aiProxy failed uid=${uid}:`, error);
      throw new functions.https.HttpsError(
        "internal",
        "AI request failed. Please try again."
      );
    }
  }
);
