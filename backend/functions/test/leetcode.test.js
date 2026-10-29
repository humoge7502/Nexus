"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");

const { normalizeUsername, mapStats } = require("../lib/leetcode/stats");

function expectHttpsError(fn, code) {
  let thrown;
  try {
    fn();
  } catch (error) {
    thrown = error;
  }
  assert.ok(thrown, "expected call to throw");
  assert.equal(thrown.code, code, `expected code ${code}, got ${thrown.code}`);
}

test("normalizeUsername trims and accepts valid handles", () => {
  assert.equal(normalizeUsername("  tourist  "), "tourist");
  assert.equal(normalizeUsername("a_b-c9"), "a_b-c9");
  assert.equal(normalizeUsername("x".repeat(40)), "x".repeat(40));
});

test("normalizeUsername rejects missing and malformed handles", () => {
  expectHttpsError(() => normalizeUsername(undefined), "invalid-argument");
  expectHttpsError(() => normalizeUsername(null), "invalid-argument");
  expectHttpsError(() => normalizeUsername(42), "invalid-argument");
  expectHttpsError(() => normalizeUsername("   "), "invalid-argument");
  expectHttpsError(() => normalizeUsername("x".repeat(41)), "invalid-argument");
  expectHttpsError(() => normalizeUsername("bad handle!"), "invalid-argument");
  expectHttpsError(() => normalizeUsername("sla/sh"), "invalid-argument");
});

test("mapStats reads per-difficulty counts and ranking", () => {
  const stats = mapStats(
    {
      data: {
        matchedUser: {
          profile: { ranking: 123456 },
          submitStats: {
            acSubmissionNum: [
              { difficulty: "All", count: 420 },
              { difficulty: "Easy", count: 180 },
              { difficulty: "Medium", count: 200 },
              { difficulty: "Hard", count: 40 },
            ],
          },
        },
      },
    },
    "tourist"
  );
  assert.deepEqual(stats, {
    totalSolved: 420,
    easySolved: 180,
    mediumSolved: 200,
    hardSolved: 40,
    ranking: 123456,
  });
});

test("mapStats derives totals when the All bucket is absent", () => {
  const stats = mapStats(
    {
      data: {
        matchedUser: {
          profile: { ranking: 0 },
          submitStats: {
            acSubmissionNum: [
              { difficulty: "Easy", count: 5 },
              { difficulty: "Medium", count: 7 },
            ],
          },
        },
      },
    },
    "someone"
  );
  assert.equal(stats.totalSolved, 12);
  assert.equal(stats.hardSolved, 0);
  assert.equal(stats.ranking, 0);
});

test("mapStats tolerates missing optional blocks", () => {
  const stats = mapStats({ data: { matchedUser: {} } }, "empty");
  assert.deepEqual(stats, {
    totalSolved: 0,
    easySolved: 0,
    mediumSolved: 0,
    hardSolved: 0,
    ranking: 0,
  });
});

test("mapStats throws not-found for unknown profiles", () => {
  expectHttpsError(() => mapStats({ data: { matchedUser: null } }, "ghost"), "not-found");
  expectHttpsError(() => mapStats({ errors: [{ message: "boom" }] }, "ghost"), "not-found");
});
