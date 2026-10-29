import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { requireAuth } from "../middleware/auth";

interface CohortRequest {
  collegeId?: string;
  companyTier?: string;
  semester?: string;
}

export function normalizeCollegeId(value: unknown): string {
  return typeof value === "string" ? value.trim().slice(0, 120) : "";
}

export function normalizeTier(value: unknown): string | undefined {
  return typeof value === "string" && value.trim().length > 0
    ? value.trim().slice(0, 60)
    : undefined;
}

export function normalizeSemester(value: unknown): string | undefined {
  return typeof value === "string" && value.trim().length > 0
    ? value.trim().slice(0, 40)
    : undefined;
}

export function projectCohort(id: string, d: FirebaseFirestore.DocumentData) {
  return {
    id,
    collegeId: d.collegeId ?? null,
    companyTier: d.companyTier ?? null,
    semester: d.semester ?? null,
    placedCount: d.placedCount ?? 0,
    totalStudents: d.totalStudents ?? 0,
    averagePackage: d.averagePackage ?? null,
    updatedAt: d.updatedAt ?? null,
  };
}

export const cohortIntelligence: functions.HttpsFunction = functions.https.onCall(
  async (data: CohortRequest, context: functions.https.CallableContext) => {
    const uid = await requireAuth(context);

    const collegeId = normalizeCollegeId(data?.collegeId);
    if (!collegeId) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "collegeId is required."
      );
    }

    try {
      const db = admin.firestore();

      // Cohort statistics are only available to students of that institution.
      const userSnap = await db.collection("users").doc(uid).get();
      const userCollege = String(
        userSnap.get("college") || userSnap.get("collegeId") || ""
      ).trim();
      if (
        !userCollege ||
        userCollege.toLowerCase() !== collegeId.toLowerCase()
      ) {
        throw new functions.https.HttpsError(
          "permission-denied",
          "Cohort intelligence is only available for your own college."
        );
      }

      const companyTier = normalizeTier(data?.companyTier);
      const semester = normalizeSemester(data?.semester);

      let query: FirebaseFirestore.Query = db.collection("cohort_intelligence");

      query = query.where("collegeId", "==", collegeId);
      if (companyTier) {
        query = query.where("companyTier", "==", companyTier);
      }
      if (semester) {
        query = query.where("semester", "==", semester);
      }

      query = query.orderBy("updatedAt", "desc").limit(50);

      const snapshot = await query.get();
      const results = snapshot.docs.map((doc) =>
        projectCohort(doc.id, doc.data())
      );

      return { results, total: results.length };
    } catch (error) {
      if (error instanceof functions.https.HttpsError) throw error;
      console.error(
        `getCohortIntelligence failed uid=${context.auth?.uid}:`,
        error instanceof Error ? error.message : String(error)
      );
      throw new functions.https.HttpsError(
        "internal",
        "Failed to fetch cohort intelligence. Try again later."
      );
    }
  }
);
