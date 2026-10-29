import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { requireAuth } from "../middleware/auth";

/**
 * LeetCode stats, served from our own backend.
 *
 * The Android client used to call a community-hosted mirror directly. This
 * function removes that dependency: it queries LeetCode's public GraphQL
 * endpoint, caches the result in Firestore for 10 minutes, and serves up to
 * 24-hour-old data when the upstream is briefly unavailable.
 */

const LEETCODE_GRAPHQL_URL = "https://leetcode.com/graphql";
const UPSTREAM_TIMEOUT_MS = 8_000;
const CACHE_TTL_MS = 10 * 60 * 1000;
const STALE_FALLBACK_MS = 24 * 60 * 60 * 1000;
const USERNAME_PATTERN = /^[A-Za-z0-9_-]{1,40}$/;

const STATS_QUERY = `
  query userStats($username: String!) {
    matchedUser(username: $username) {
      profile { ranking }
      submitStats {
        acSubmissionNum { difficulty count }
      }
    }
  }
`;

export interface LeetCodeStats {
  totalSolved: number;
  easySolved: number;
  mediumSolved: number;
  hardSolved: number;
  ranking: number;
}

interface GraphQLResponse {
  data?: {
    matchedUser?: {
      profile?: { ranking?: number };
      submitStats?: {
        acSubmissionNum?: Array<{ difficulty?: string; count?: number }>;
      };
    } | null;
  };
  errors?: Array<{ message?: string }>;
}

interface CacheEntry {
  username: string;
  stats: LeetCodeStats;
  fetchedAt: number;
}

export function normalizeUsername(raw: unknown): string {
  if (typeof raw !== "string") {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "A LeetCode username is required."
    );
  }
  const username = raw.trim();
  if (!USERNAME_PATTERN.test(username)) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Usernames may contain letters, numbers, underscores and hyphens (max 40)."
    );
  }
  return username;
}

export function mapStats(payload: GraphQLResponse, username: string): LeetCodeStats {
  const user = payload.data?.matchedUser;
  if (!user) {
    throw new functions.https.HttpsError(
      "not-found",
      `No public LeetCode profile found for "${username}".`
    );
  }
  const counts = new Map<string, number>();
  for (const entry of user.submitStats?.acSubmissionNum ?? []) {
    if (entry.difficulty) counts.set(entry.difficulty, Number(entry.count ?? 0));
  }
  const easy = counts.get("Easy") ?? 0;
  const medium = counts.get("Medium") ?? 0;
  const hard = counts.get("Hard") ?? 0;
  const total = counts.get("All") ?? easy + medium + hard;
  return {
    totalSolved: total,
    easySolved: easy,
    mediumSolved: medium,
    hardSolved: hard,
    ranking: Number(user.profile?.ranking ?? 0),
  };
}

async function fetchStats(username: string): Promise<LeetCodeStats> {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), UPSTREAM_TIMEOUT_MS);
  try {
    const response = await fetch(LEETCODE_GRAPHQL_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Referer: "https://leetcode.com",
        "User-Agent": "NEXUS-App/2.0 (+https://nexusapp.in)",
      },
      body: JSON.stringify({
        query: STATS_QUERY,
        variables: { username },
        operationName: "userStats",
      }),
      signal: controller.signal,
    });
    if (!response.ok) {
      throw new Error(`LeetCode responded with ${response.status}`);
    }
    return mapStats((await response.json()) as GraphQLResponse, username);
  } finally {
    clearTimeout(timeout);
  }
}

function isFresh(entry: CacheEntry, maxAgeMs: number): boolean {
  return Date.now() - entry.fetchedAt < maxAgeMs;
}

export const leetcodeStats: functions.HttpsFunction = functions.https.onCall(
  async (data: { username?: string }, context: functions.https.CallableContext) => {
    await requireAuth(context);
    const username = normalizeUsername(data?.username);
    const doc = admin
      .firestore()
      .collection("leetcode_stats")
      .doc(username.toLowerCase());

    const snapshot = await doc.get().catch(() => null);
    const cached = snapshot?.exists ? (snapshot.data() as CacheEntry) : null;
    if (cached && isFresh(cached, CACHE_TTL_MS)) {
      return { ...cached.stats, cached: true };
    }

    try {
      const stats = await fetchStats(username);
      await doc
        .set({ username, stats, fetchedAt: Date.now() } satisfies CacheEntry)
        .catch((error) => {
          console.warn("leetcodeStats cache write failed", error);
        });
      return { ...stats, cached: false };
    } catch (error) {
      console.error(
        `leetcodeStats upstream failure user=${username}`,
        error instanceof Error ? error.message : error
      );
      if (cached && isFresh(cached, STALE_FALLBACK_MS)) {
        return { ...cached.stats, cached: true, stale: true };
      }
      throw new functions.https.HttpsError(
        "unavailable",
        "LeetCode stats are temporarily unavailable. Try again shortly."
      );
    }
  }
);
