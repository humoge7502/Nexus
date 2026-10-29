import * as functions from "firebase-functions";

export const healthCheck: functions.HttpsFunction = functions.https.onCall(
  async (_data: unknown, _context: functions.https.CallableContext) => {
    return {
      status: "ok",
      timestamp: Date.now(),
      version: "2.0.0",
    };
  }
);
