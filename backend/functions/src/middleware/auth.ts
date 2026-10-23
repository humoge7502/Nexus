import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

export async function requireAuth(context: functions.https.CallableContext): Promise<string> {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Authentication required. Send a valid Firebase ID token."
    );
  }

  try {
    const uid = context.auth.uid;
    await admin.auth().getUser(uid);
    return uid;
  } catch (error) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Invalid or expired Firebase ID token."
    );
  }
}

export async function maybeAuth(
  context: functions.https.CallableContext
): Promise<string | null> {
  if (!context.auth) return null;
  try {
    return context.auth.uid;
  } catch {
    return null;
  }
}
