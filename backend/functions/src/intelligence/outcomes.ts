import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { requireAuth } from "../middleware/auth";

interface OutcomesRequest {
  company?: string;
  limit?: number;
}

export const intelligenceOutcomes: functions.HttpsFunction = functions.https.onCall(
  async (data: OutcomesRequest, context: functions.https.CallableContext) => {
    await requireAuth(context);

    const limit = Math.min(data.limit || 20, 100);

    try {
      const db = admin.firestore();
      let query: FirebaseFirestore.Query = db
        .collection("interview_outcomes")
        .where("isAnonymized", "==", true)
        .orderBy("reportedAt", "desc")
        .limit(limit);

      if (data.company) {
        query = query.where("company", "==", data.company);
      }

      const snapshot = await query.get();
      const results = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      }));

      return { results, total: results.length };
    } catch (error) {
      console.error(
        "getIntelligenceOutcomes failed uid=${context.auth?.uid}:",
        error instanceof Error ? error.message : String(error)
      );
      throw new functions.https.HttpsError(
        "internal",
        "Failed to fetch intelligence outcomes. Try again later."
      );
    }
  }
);
