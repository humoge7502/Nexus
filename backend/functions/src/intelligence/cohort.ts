import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { requireAuth } from "../middleware/auth";

interface CohortRequest {
  collegeId?: string;
  companyTier?: string;
  semester?: string;
}

export const cohortIntelligence: functions.HttpsFunction = functions.https.onCall(
  async (data: CohortRequest, context: functions.https.CallableContext) => {
    await requireAuth(context);

    try {
      const db = admin.firestore();
      let query: FirebaseFirestore.Query = db.collection("cohort_intelligence");

      if (data.collegeId) {
        query = query.where("collegeId", "==", data.collegeId);
      }
      if (data.companyTier) {
        query = query.where("companyTier", "==", data.companyTier);
      }
      if (data.semester) {
        query = query.where("semester", "==", data.semester);
      }

      query = query.orderBy("updatedAt", "desc").limit(50);

      const snapshot = await query.get();
      const results = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      }));

      return { results, total: results.length };
    } catch (error) {
      throw new functions.https.HttpsError(
        "internal",
        `Failed to fetch cohort intelligence: ${error instanceof Error ? error.message : String(error)}`
      );
    }
  }
);
