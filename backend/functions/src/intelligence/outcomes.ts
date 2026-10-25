import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { requireAuth } from "../middleware/auth";

interface OutcomesRequest {
  company?: string;
  limit?: number;
}

export function clampOutcomeLimit(value: unknown): number {
  return Math.min(Math.max(Number(value) || 20, 1), 100);
}

export function normalizeCompany(value: unknown): string | undefined {
  return typeof value === "string" && value.trim().length > 0
    ? value.trim().slice(0, 80)
    : undefined;
}

// reporterId and other internal identifiers must never leave the server,
// even on documents marked anonymized.
export function projectOutcome(id: string, d: FirebaseFirestore.DocumentData) {
  return {
    id,
    company: d.company ?? null,
    role: d.role ?? null,
    tier: d.tier ?? null,
    rounds: Array.isArray(d.rounds) ? d.rounds : [],
    outcome: d.outcome ?? null,
    difficulty: d.difficulty ?? null,
    tips: Array.isArray(d.tips) ? d.tips : [],
    reportedAt: d.reportedAt ?? null,
  };
}

export const intelligenceOutcomes: functions.HttpsFunction = functions.https.onCall(
  async (data: OutcomesRequest, context: functions.https.CallableContext) => {
    await requireAuth(context);

    const limit = clampOutcomeLimit(data?.limit);
    const company = normalizeCompany(data?.company);

    try {
      const db = admin.firestore();
      let query: FirebaseFirestore.Query = db
        .collection("interview_outcomes")
        .where("isAnonymized", "==", true)
        .orderBy("reportedAt", "desc")
        .limit(limit);

      if (company) {
        query = query.where("company", "==", company);
      }

      const snapshot = await query.get();
      const results = snapshot.docs.map((doc) =>
        projectOutcome(doc.id, doc.data())
      );

      return { results, total: results.length };
    } catch (error) {
      console.error(
        `getIntelligenceOutcomes failed uid=${context.auth?.uid}:`,
        error instanceof Error ? error.message : String(error)
      );
      throw new functions.https.HttpsError(
        "internal",
        "Failed to fetch intelligence outcomes. Try again later."
      );
    }
  }
);
