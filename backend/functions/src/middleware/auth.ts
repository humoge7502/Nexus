import * as functions from "firebase-functions";

/**
 * Callable functions already receive a verified Firebase ID token in
 * `context.auth`; the SDK validates signature, audience and expiry. An extra
 * Admin SDK `getUser` round-trip added latency to every request without
 * adding security.
 */
export async function requireAuth(context: functions.https.CallableContext): Promise<string> {
  if (!context.auth?.uid) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Authentication required. Send a valid Firebase ID token."
    );
  }
  return context.auth.uid;
}

export async function maybeAuth(
  context: functions.https.CallableContext
): Promise<string | null> {
  return context.auth?.uid ?? null;
}
