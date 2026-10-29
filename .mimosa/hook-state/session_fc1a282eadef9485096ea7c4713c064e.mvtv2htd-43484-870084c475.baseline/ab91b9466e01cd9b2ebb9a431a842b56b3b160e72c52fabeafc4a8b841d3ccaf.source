import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { FieldValue } from "firebase-admin/firestore";
import {
  AIRequest,
  recordTokens,
  reserveQuota,
  validateRequest,
} from "./proxy";

/**
 * Authenticated SSE bridge to OpenRouter.
 *
 * Route-level controls mirror proxyAI: ID-token auth, model allowlist and
 * prompt caps via validateRequest, and the same per-user daily quota. The
 * upstream key never reaches the client and upstream error bodies are only
 * logged server-side.
 */

const OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
const UPSTREAM_TIMEOUT_MS = 90_000;
const MAX_USAGE_SCAN_CHARS = 200_000;

type ErrorStatus =
  | "UNAUTHENTICATED"
  | "INVALID_ARGUMENT"
  | "RESOURCE_EXHAUSTED"
  | "FAILED_PRECONDITION"
  | "UNAVAILABLE"
  | "METHOD_NOT_ALLOWED";

interface HttpErrorBody {
  error: { status: ErrorStatus; message: string };
}

export function bearerToken(header?: string): string | null {
  if (!header) return null;
  const match = /^Bearer\s+(.+)$/i.exec(header.trim());
  return match ? match[1].trim() : null;
}

export function statusForHttpsError(code: string): {
  httpStatus: number;
  errorStatus: ErrorStatus;
} {
  switch (code) {
    case "resource-exhausted":
      return { httpStatus: 429, errorStatus: "RESOURCE_EXHAUSTED" };
    case "invalid-argument":
      return { httpStatus: 400, errorStatus: "INVALID_ARGUMENT" };
    case "unauthenticated":
      return { httpStatus: 401, errorStatus: "UNAUTHENTICATED" };
    case "failed-precondition":
      return { httpStatus: 503, errorStatus: "FAILED_PRECONDITION" };
    default:
      return { httpStatus: 502, errorStatus: "UNAVAILABLE" };
  }
}

function sendJsonError(
  res: functions.Response,
  status: number,
  errorStatus: ErrorStatus,
  message: string
): void {
  if (res.headersSent) return;
  const body: HttpErrorBody = { error: { status: errorStatus, message } };
  res.status(status).set("Content-Type", "application/json; charset=utf-8").json(body);
}

export function lastReportedTokens(text: string): number {
  let tokens = 0;
  for (const match of text.matchAll(/"total_tokens"\s*:\s*(\d+)/g)) {
    tokens = Number(match[1]);
  }
  return tokens;
}

export const aiStream = functions
  .runWith({ timeoutSeconds: 120, memory: "256MB" })
  .https.onRequest(async (req, res) => {
    if (req.method !== "POST") {
      res.set("Allow", "POST");
      sendJsonError(res, 405, "METHOD_NOT_ALLOWED", "Use POST.");
      return;
    }

    const token = bearerToken(req.get("authorization") ?? undefined);
    if (!token) {
      sendJsonError(res, 401, "UNAUTHENTICATED", "Sign in required.");
      return;
    }

    let uid: string;
    try {
      uid = (await admin.auth().verifyIdToken(token)).uid;
    } catch {
      sendJsonError(res, 401, "UNAUTHENTICATED", "Invalid or expired token.");
      return;
    }

    let requestBody: AIRequest;
    try {
      requestBody = validateRequest(req.body as AIRequest);
    } catch (error) {
      const message =
        error instanceof functions.https.HttpsError
          ? error.message
          : "Invalid request body.";
      sendJsonError(res, 400, "INVALID_ARGUMENT", message);
      return;
    }

    const apiKey = process.env.OPENROUTER_API_KEY;
    if (!apiKey) {
      sendJsonError(res, 503, "FAILED_PRECONDITION", "AI service is not configured.");
      return;
    }

    let usageDocId: string;
    try {
      usageDocId = await reserveQuota(uid);
    } catch (error) {
      if (error instanceof functions.https.HttpsError) {
        const mapped = statusForHttpsError(error.code);
        sendJsonError(res, mapped.httpStatus, mapped.errorStatus, error.message);
      } else {
        console.error(`aiStream quota failed uid=${uid}`);
        sendJsonError(res, 502, "UNAVAILABLE", "AI service is temporarily unavailable.");
      }
      return;
    }

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), UPSTREAM_TIMEOUT_MS);

    let upstream: globalThis.Response;
    try {
      upstream = await fetch(OPENROUTER_URL, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${apiKey}`,
          "Content-Type": "application/json",
          "HTTP-Referer": "https://nexusapp.in",
          "X-Title": "NEXUS",
        },
        body: JSON.stringify({
          ...requestBody,
          stream: true,
          stream_options: { include_usage: true },
        }),
        signal: controller.signal,
      });
    } catch (error) {
      clearTimeout(timeout);
      const aborted = controller.signal.aborted;
      console.error(
        `aiStream upstream failed uid=${uid} aborted=${aborted}:`,
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
      sendJsonError(res, 502, "UNAVAILABLE", "AI service is temporarily unavailable.");
      return;
    }

    if (!upstream.ok || !upstream.body) {
      const errorBody = await upstream.text().catch(() => "");
      console.error(
        `aiStream upstream error uid=${uid} status=${upstream.status} body=${errorBody.slice(0, 500)}`
      );
      clearTimeout(timeout);
      await admin
        .firestore()
        .collection("ai_proxy_logs")
        .add({
          uid,
          model: requestBody.model,
          outcome: "error",
          timestamp: FieldValue.serverTimestamp(),
        })
        .catch(() => undefined);
      sendJsonError(res, 502, "UNAVAILABLE", "AI service is temporarily unavailable.");
      return;
    }

    res.status(200).set({
      "Content-Type": "text/event-stream; charset=utf-8",
      "Cache-Control": "no-cache, no-transform",
      Connection: "keep-alive",
      "X-Accel-Buffering": "no",
    });
    res.flushHeaders();

    let clientClosed = false;
    res.on("close", () => {
      clientClosed = true;
      controller.abort();
    });

    let scanned = "";
    let totalTokens = 0;
    try {
      const reader = upstream.body.getReader();
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        if (clientClosed) {
          await reader.cancel().catch(() => undefined);
          break;
        }
        const chunk = Buffer.from(value);
        res.write(chunk);
        if (scanned.length < MAX_USAGE_SCAN_CHARS) {
          scanned += chunk.toString("utf8");
        }
      }
      totalTokens = lastReportedTokens(scanned);
      res.end();
    } catch (error) {
      if (!clientClosed) {
        console.error(
          `aiStream interrupted uid=${uid}:`,
          error instanceof Error ? error.message : String(error)
        );
        if (!res.writableEnded) {
          res.write(
            `event: error\ndata: ${JSON.stringify({
              message: "AI stream interrupted.",
            })}\n\n`
          );
          res.end();
        }
      }
    } finally {
      clearTimeout(timeout);
      await recordTokens(usageDocId, totalTokens).catch(() => undefined);
      await admin
        .firestore()
        .collection("ai_proxy_logs")
        .add({
          uid,
          model: requestBody.model,
          totalTokens,
          outcome: clientClosed ? "cancelled" : "success",
          timestamp: FieldValue.serverTimestamp(),
        })
        .catch(() => undefined);
    }
  });
