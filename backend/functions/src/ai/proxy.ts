import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { requireAuth } from "../middleware/auth";

const OPENROUTER_API_KEY = (() => {
  const key = process.env.OPENROUTER_API_KEY;
  if (!key) {
    throw new Error("OPENROUTER_API_KEY environment variable not set");
  }
  return key;
})();

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

export const aiProxy: functions.HttpsFunction = functions.https.onCall(
  async (data: AIRequest, context: functions.https.CallableContext) => {
    await requireAuth(context);

    const requestBody: AIRequest = {
      model: data.model || "openai/gpt-4o-mini",
      messages: data.messages,
      max_tokens: data.max_tokens || 2048,
      temperature: data.temperature ?? 0.7,
    };

    try {
      const response = await fetch(OPENROUTER_URL, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${OPENROUTER_API_KEY}`,
          "Content-Type": "application/json",
          "HTTP-Referer": "https://nexusapp.in",
          "X-Title": "NEXUS",
        },
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        const errorBody = await response.text();
        throw new functions.https.HttpsError(
          "internal",
          `OpenRouter API error (${response.status}): ${errorBody}`
        );
      }

      const result = (await response.json()) as AIResponse;

      await admin.firestore().collection("ai_proxy_logs").add({
        uid: context.auth!.uid,
        model: requestBody.model,
        promptTokens: result.usage?.prompt_tokens ?? 0,
        completionTokens: result.usage?.completion_tokens ?? 0,
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
      throw new functions.https.HttpsError(
        "internal",
        `AI proxy request failed: ${error instanceof Error ? error.message : String(error)}`
      );
    }
  }
);
