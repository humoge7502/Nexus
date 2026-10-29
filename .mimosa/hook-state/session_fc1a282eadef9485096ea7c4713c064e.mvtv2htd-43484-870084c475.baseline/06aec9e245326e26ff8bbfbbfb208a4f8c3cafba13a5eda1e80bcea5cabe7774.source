import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { requireAuth } from "../middleware/auth";

interface FeedRequest {
  opportunityType: "internship" | "certification" | "scholarship" | "hackathon" | "all";
  limit?: number;
  offset?: number;
  skills?: string[];
}

const ALLOWED_TYPES = new Set([
  "internship",
  "certification",
  "scholarship",
  "hackathon",
  "all",
]);
const MAX_OFFSET = 1_000;
const MAX_LIMIT = 50;
const MAX_SKILLS = 10;

export function clampLimit(value: unknown): number {
  return Math.min(Math.max(Number(value) || 20, 1), MAX_LIMIT);
}

export function clampOffset(value: unknown): number {
  return Math.min(Math.max(Number(value) || 0, 0), MAX_OFFSET);
}

export function sanitizeSkills(value: unknown): string[] {
  if (!Array.isArray(value)) return [];
  return value
    .filter((s): s is string => typeof s === "string" && s.trim().length > 0)
    .map((s) => s.trim())
    .slice(0, MAX_SKILLS);
}

// Seeded documents use role/applicationUrl while the scraper writes both
// naming conventions; project with fallbacks so neither source loses data.
export function projectOpportunity(id: string, d: FirebaseFirestore.DocumentData) {
  return {
    id,
    type: d.type,
    title: d.title ?? d.role ?? "",
    role: d.role ?? d.title ?? "",
    company: d.company ?? "",
    location: d.location ?? "",
    description: d.description ?? "",
    applyUrl: d.applyUrl ?? d.applicationUrl ?? "",
    applicationUrl: d.applicationUrl ?? d.applyUrl ?? "",
    deadline: d.deadline ?? null,
    stipend: d.stipend ?? "",
    requiredSkills: d.requiredSkills ?? [],
    tags: d.tags ?? [],
    isActive: d.isActive ?? true,
    createdAt: d.createdAt ?? null,
  };
}

export const opportunityFeed: functions.HttpsFunction = functions.https.onCall(
  async (data: FeedRequest, context: functions.https.CallableContext) => {
    await requireAuth(context);

    const requestedType = (data?.opportunityType || "all").toLowerCase();
    if (!ALLOWED_TYPES.has(requestedType)) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "opportunityType is not supported."
      );
    }
    const limit = clampLimit(data?.limit);
    const offset = clampOffset(data?.offset);
    const opportunityType = requestedType;
    const skills = sanitizeSkills(data?.skills);

    try {
      const db = admin.firestore();
      let query: FirebaseFirestore.Query = db.collection("opportunities");

      if (opportunityType !== "all") {
        query = query.where("type", "==", opportunityType.toUpperCase());
      }

      if (skills.length > 0) {
        query = query.where("requiredSkills", "array-contains-any", skills);
      }

      query = query
        .orderBy("createdAt", "desc")
        .offset(offset)
        .limit(limit);

      const snapshot = await query.get();
      const results = snapshot.docs.map((doc) =>
        projectOpportunity(doc.id, doc.data())
      );

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
