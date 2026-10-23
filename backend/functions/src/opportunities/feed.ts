import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { requireAuth } from "../middleware/auth";

interface FeedRequest {
  opportunityType: "internship" | "certification" | "scholarship" | "hackathon" | "all";
  limit?: number;
  offset?: number;
  skills?: string[];
}

// Seeded documents store `type` in UPPERCASE ("INTERNSHIP", ...) and skills
// under `requiredSkills` (see backend/seed/opportunities.json).
export const opportunityFeed: functions.HttpsFunction = functions.https.onCall(
  async (data: FeedRequest, context: functions.https.CallableContext) => {
    await requireAuth(context);

    const limit = Math.min(Math.max(data.limit || 20, 1), 50);
    const offset = Math.max(data.offset || 0, 0);
    const opportunityType = data.opportunityType || "all";

    try {
      const db = admin.firestore();
      let query: FirebaseFirestore.Query = db.collection("opportunities");

      if (opportunityType !== "all") {
        query = query.where("type", "==", opportunityType.toUpperCase());
      }

      if (data.skills && data.skills.length > 0) {
        query = query.where(
          "requiredSkills",
          "array-contains-any",
          data.skills.slice(0, 10)
        );
      }

      query = query
        .orderBy("createdAt", "desc")
        .offset(offset)
        .limit(limit);

      const snapshot = await query.get();
      const results = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      }));

      return {
        results,
        total: results.length,
        offset: offset + results.length,
      };
    } catch (error) {
      console.error(
        `opportunityFeed failed uid=${context.auth?.uid}:`,
        error instanceof Error ? error.message : String(error)
      );
      throw new functions.https.HttpsError(
        "internal",
        "Failed to fetch opportunity feed. Try again later."
      );
    }
  }
);
