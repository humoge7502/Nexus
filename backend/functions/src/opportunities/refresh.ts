import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

/**
 * Daily opportunity freshness job (Cloud Scheduler, IST morning).
 *
 * Two responsibilities, both deterministic and safe to re-run:
 *  1. Expiry sweep — opportunities whose deadline has passed are marked
 *     isActive=false so the app feed stops surfacing dead listings.
 *  2. Health snapshot — counts + lastRefresh written to meta/opportunities,
 *     which the app can surface as "updated Xh ago".
 *
 * External scraping is deliberately NOT done here: listing sources need a
 * data-source decision (ToS, rate limits, parsing maintenance). This job
 * keeps the existing collection correct and measurable every morning.
 */

export interface ExpiryCandidate {
  id: string;
  deadline?: number | string | null;
  isActive?: boolean;
}

export interface ExpiryDecision {
  toDeactivate: string[];
  keptActive: number;
}

/** Deadline semantics: epoch millis, ISO string, or null (never expires). */
export function isExpired(deadline: number | string | null | undefined, now: number): boolean {
  if (deadline === null || deadline === undefined) return false;
  if (typeof deadline === "number") {
    return Number.isFinite(deadline) && deadline < now;
  }
  const parsed = Date.parse(deadline);
  return Number.isFinite(parsed) && parsed < now;
}

export function decideExpiries(candidates: ExpiryCandidate[], now: number): ExpiryDecision {
  const toDeactivate: string[] = [];
  let keptActive = 0;
  for (const c of candidates) {
    if (c.isActive === false) continue; // already inactive — nothing to do
    if (isExpired(c.deadline, now)) {
      toDeactivate.push(c.id);
    } else {
      keptActive += 1;
    }
  }
  return { toDeactivate, keptActive };
}

export const refreshOpportunities: functions.CloudFunction<void> = functions.pubsub
  .schedule("every day 06:00")
  .timeZone("Asia/Kolkata")
  .onRun(async () => {
    const db = admin.firestore();
    const now = Date.now();

    const snapshot = await db.collection("opportunities").get();
    const candidates: ExpiryCandidate[] = snapshot.docs.map((d) => {
      const data = d.data();
      return {
        id: d.id,
        deadline: (data.deadline ?? null) as number | string | null,
        isActive: (data.isActive ?? true) as boolean,
      };
    });

    const decision = decideExpiries(candidates, now);

    // Firestore batches cap at 500 writes; chunk the deactivations.
    const CHUNK = 400;
    for (let i = 0; i < decision.toDeactivate.length; i += CHUNK) {
      const batch = db.batch();
      for (const id of decision.toDeactivate.slice(i, i + CHUNK)) {
        batch.update(db.collection("opportunities").doc(id), {
          isActive: false,
          expiredAt: now,
        });
      }
      await batch.commit();
    }

    await db.doc("meta/opportunities").set({
      lastRefreshAt: now,
      total: candidates.length,
      active: decision.keptActive,
      expiredThisRun: decision.toDeactivate.length,
    });

    console.log(
      `refreshOpportunities: scanned=${candidates.length} ` +
        `deactivated=${decision.toDeactivate.length} active=${decision.keptActive}`
    );
  });
