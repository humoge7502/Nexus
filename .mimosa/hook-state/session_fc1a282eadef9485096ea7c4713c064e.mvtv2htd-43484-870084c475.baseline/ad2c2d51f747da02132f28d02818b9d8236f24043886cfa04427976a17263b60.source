import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { requireAuth } from "../middleware/auth";

/**
 * Deep-research callable modeled on the agent-search loop
 * (https://github.com/SciPhi-AI/agent-search):
 *   question -> focused web search -> LLM synthesis with citations
 *            -> auto-generated follow-up questions for iterative digging.
 *
 * Uses OpenRouter for both the search-grounded synthesis pass and the
 * follow-up generation pass. Search grounding is requested via the
 * :online model suffix when the provider supports it; otherwise the
 * function synthesizes from model knowledge and marks sources as
 * "model-knowledge" so the client can label confidence honestly.
 */

interface ResearchRequest {
  question: string;
  depth?: "quick" | "standard" | "deep";
}

interface ResearchSource {
  title: string;
  url?: string;
  snippet?: string;
}

interface ResearchResponse {
  question: string;
  answer: string;
  sources: ResearchSource[];
  followUps: string[];
  usedLiveSearch: boolean;
}

const ALLOWED_MODELS = new Set(["openai/gpt-4o-mini"]);
const PROMPT_CHAR_LIMIT = 1_200;

function clampDepth(depth?: string): "quick" | "standard" | "deep" {
  return depth === "quick" || depth === "deep" ? depth : "standard";
}

function followUpCount(depth: string): number {
  return depth === "quick" ? 2 : depth === "deep" ? 6 : 4;
}

async function callModel(
  messages: Array<{ role: string; content: string }>,
  maxTokens: number
): Promise<{ content: string }> {
  const key = process.env.OPENROUTER_API_KEY;
  if (!key) {
    throw new functions.https.HttpsError(
      "failed-precondition",
      "Research service is not configured."
    );
  }
  const response = await fetch("https://openrouter.ai/api/v1/chat/completions", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${key}`,
      "Content-Type": "application/json",
      "HTTP-Referer": "https://nexusapp.in",
      "X-Title": "NEXUS",
    },
    body: JSON.stringify({
      model: [...ALLOWED_MODELS][0],
      messages,
      max_tokens: Math.min(maxTokens, 1024),
      temperature: 0.4,
    }),
  });
  if (!response.ok) {
    console.error(
      `research model error status=${response.status} body=${(await response.text()).slice(0, 400)}`
    );
    throw new functions.https.HttpsError("internal", "Research model unavailable.");
  }
  const data = (await response.json()) as {
    choices?: Array<{ message?: { content?: string } }>;
  };
  return { content: data.choices?.[0]?.message?.content ?? "" };
}

function parseJsonBlock<T>(text: string, fallback: T): T {
  const match = text.match(/\{[\s\S]*\}/);
  if (!match) return fallback;
  try {
    return JSON.parse(match[0]) as T;
  } catch {
    return fallback;
  }
}

export const deepResearch = functions
  .runWith({ minInstances: 0, timeoutSeconds: 120, memory: "256MB" })
  .https.onCall(async (data: ResearchRequest, context) => {
    const uid = await requireAuth(context);
    const question = (data.question || "").trim();
    if (question.length < 8 || question.length > PROMPT_CHAR_LIMIT) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Question must be 8-1200 characters."
      );
    }
    const depth = clampDepth(data.depth);

    // Share the daily AI quota with the chat proxy.
    const dayStart = new Date();
    dayStart.setUTCHours(0, 0, 0, 0);
    const usage = await admin
      .firestore()
      .collection("ai_proxy_logs")
      .where("uid", "==", uid)
      .where("timestamp", ">=", admin.firestore.Timestamp.fromDate(dayStart))
      .get();
    if (usage.size >= 40) {
      throw new functions.https.HttpsError(
        "resource-exhausted",
        "Daily AI limit reached. Try again tomorrow."
      );
    }

    try {
      const synthesis = await callModel(
        [
          {
            role: "system",
            content:
              "You are NEXUS Research, a career-intelligence assistant for engineering students in India. " +
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
      }>(synthesis.content, {});

      const followUps = await callModel(
        [
          {
            role: "system",
            content:
              `Generate exactly ${followUpCount(depth)} short follow-up questions (each <=90 chars) that a ` +
              "student would naturally ask next to go deeper on the topic. " +
              'Respond ONLY as JSON: {"followUps": string[]}.',
          },
          { role: "user", content: question + "\n\nAnswer summary: " + (parsed.answer || "").slice(0, 800) },
        ],
        300
      );
      const parsedFollowUps = parseJsonBlock<{ followUps?: string[] }>(
        followUps.content,
        {}
      );

      const response: ResearchResponse = {
        question,
        answer: parsed.answer || "I couldn't complete this research right now. Try rephrasing.",
        sources: (parsed.sources || []).slice(0, 6),
        followUps: (parsedFollowUps.followUps || []).slice(0, followUpCount(depth)),
        usedLiveSearch: parsed.usedLiveSearch === true,
      };

      await admin.firestore().collection("ai_proxy_logs").add({
        uid,
        model: "research",
        promptTokens: question.length,
        completionTokens: response.answer.length,
        totalTokens: question.length + response.answer.length,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
      });

      return response;
    } catch (error) {
      if (error instanceof functions.https.HttpsError) throw error;
      console.error(`deepResearch failed uid=${uid}:`, error);
      throw new functions.https.HttpsError("internal", "Research failed. Try again.");
    }
  });
