import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { maybeAuth } from "../middleware/auth";

interface FeedRequest {
  opportunityType: "internship" | "certification" | "scholarship" | "hackathon" | "all";
  limit?: number;
  offset?: number;
  skills?: string[];
}

export const opportunityFeed: functions.HttpsFunction = functions.https.onCall(
  async (data: FeedRequest, context: functions.https.CallableContext) => {
    const uid = await maybeAuth(context);

    const limit = Math.min(data.limit || 20, 50);
    const offset = data.offset || 0;
    const opportunityType = data.opportunityType || "all";

    try {
      const db = admin.firestore();
      let query: FirebaseFirestore.Query = db.collection("opportunities");

      if (opportunityType !== "all") {
        query = query.where("type", "==", opportunityType);
      }

      if (data.skills && data.skills.length > 0) {
        query = query.where("skills", "array-contains-any", data.skills.slice(0, 10));
      }

      query = query.orderBy("createdAt", "desc").limit(limit);

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
      throw new functions.https.HttpsError(
        "internal",
        `Failed to fetch opportunity feed: ${error instanceof Error ? error.message : String(error)}`
      );
    }
  }
);
