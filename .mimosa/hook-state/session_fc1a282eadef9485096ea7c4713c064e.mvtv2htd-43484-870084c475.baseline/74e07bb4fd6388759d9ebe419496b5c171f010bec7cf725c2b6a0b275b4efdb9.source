"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");

const {
  validateRequest,
  usageDocId,
  PROMPT_LIMITS,
} = require("../lib/ai/proxy");
const {
  clampLimit,
  clampOffset,
  sanitizeSkills,
  projectOpportunity,
} = require("../lib/opportunities/feed");
const {
  clampOutcomeLimit,
  normalizeCompany,
  projectOutcome,
} = require("../lib/intelligence/outcomes");
const {
  normalizeCollegeId,
  normalizeTier,
  normalizeSemester,
  projectCohort,
} = require("../lib/intelligence/cohort");

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

test("validateRequest rejects malformed payloads", () => {
  expectHttpsError(() => validateRequest(null), "invalid-argument");
  expectHttpsError(() => validateRequest({}), "invalid-argument");
  expectHttpsError(() => validateRequest({ messages: [] }), "invalid-argument");
  expectHttpsError(
    () => validateRequest({ model: "openai/gpt-4o", messages: [{ role: "user", content: "hi" }] }),
    "invalid-argument"
  );
});

test("validateRequest rejects oversized prompts and too many messages", () => {
  const many = Array.from({ length: PROMPT_LIMITS.maxMessages + 1 }, () => ({
    role: "user",
    content: "x",
  }));
  expectHttpsError(() => validateRequest({ messages: many }), "invalid-argument");

  const huge = "a".repeat(PROMPT_LIMITS.maxMessageChars + 1);
  expectHttpsError(
    () => validateRequest({ messages: [{ role: "user", content: huge }] }),
    "invalid-argument"
  );
});

test("validateRequest rejects disallowed roles and misplaced system prompts", () => {
  expectHttpsError(
    () => validateRequest({ messages: [{ role: "developer", content: "x" }] }),
    "invalid-argument"
  );
  expectHttpsError(
    () =>
      validateRequest({
        messages: [
          { role: "user", content: "hi" },
          { role: "system", content: "late system" },
        ],
      }),
    "invalid-argument"
  );
});

test("validateRequest clamps tokens and temperature", () => {
  const low = validateRequest({
    messages: [{ role: "user", content: "hi" }],
    max_tokens: 0,
    temperature: -3,
  });
  assert.equal(low.max_tokens, 1);
  assert.equal(low.temperature, 0);
  assert.equal(low.model, "openai/gpt-4o-mini");

  const high = validateRequest({
    messages: [{ role: "user", content: "hi" }],
    max_tokens: 999_999,
    temperature: 9,
  });
  assert.equal(high.max_tokens, PROMPT_LIMITS.maxTokensPerRequest);
  assert.equal(high.temperature, 2);
});

test("validateRequest normalizes non-string content instead of crashing", () => {
  const out = validateRequest({
    messages: [{ role: "user", content: { nested: true } }],
  });
  assert.equal(out.messages[0].content, "");
});

test("usageDocId is stable per user per UTC day", () => {
  const a = usageDocId("uid1", new Date("2026-10-24T01:00:00Z"));
  const b = usageDocId("uid1", new Date("2026-10-24T23:59:59Z"));
  const c = usageDocId("uid1", new Date("2026-10-25T00:00:00Z"));
  assert.equal(a, "uid1_2026-10-24");
  assert.equal(a, b);
  assert.notEqual(b, c);
});

test("feed clamps pagination and sanitizes skills", () => {
  assert.equal(clampLimit(undefined), 20);
  assert.equal(clampLimit(500), 50);
  assert.equal(clampLimit(-5), 1);
  assert.equal(clampOffset(undefined), 0);
  assert.equal(clampOffset(5000), 1000);
  assert.equal(clampOffset(-1), 0);

  assert.deepEqual(sanitizeSkills("not-an-array"), []);
  assert.deepEqual(
    sanitizeSkills([" Kotlin ", "", 42, "Android", "Kotlin"]),
    ["Kotlin", "Android", "Kotlin"]
  );
  assert.equal(sanitizeSkills(Array.from({ length: 20 }, (_, i) => `s${i}`)).length, 10);
});

test("projectOpportunity bridges field naming between seed and scraper", () => {
  const seeded = projectOpportunity("a", {
    type: "INTERNSHIP",
    role: "SWE Intern",
    applicationUrl: "https://apply.example/a",
  });
  assert.equal(seeded.title, "SWE Intern");
  assert.equal(seeded.role, "SWE Intern");
  assert.equal(seeded.applyUrl, "https://apply.example/a");
  assert.equal(seeded.applicationUrl, "https://apply.example/a");
  assert.equal(seeded.isActive, true);
  assert.deepEqual(seeded.requiredSkills, []);

  const scraped = projectOpportunity("b", {
    type: "INTERNSHIP",
    title: "Backend Intern",
    applyUrl: "https://apply.example/b",
    isActive: false,
  });
  assert.equal(scraped.role, "Backend Intern");
  assert.equal(scraped.applicationUrl, "https://apply.example/b");
  assert.equal(scraped.isActive, false);
});

test("outcomes clamp limit, normalize company, and drop reporter identity", () => {
  assert.equal(clampOutcomeLimit(undefined), 20);
  assert.equal(clampOutcomeLimit(1000), 100);
  assert.equal(clampOutcomeLimit(0), 20);
  assert.equal(normalizeCompany("   "), undefined);
  assert.equal(normalizeCompany("  Google  "), "Google");
  assert.equal(normalizeCompany("x".repeat(200)).length, 80);

  const projected = projectOutcome("id1", {
    uid: "secret-uid",
    reporterId: "secret-reporter",
    company: "Google",
    rounds: ["DSA"],
    tips: "not-an-array",
  });
  assert.equal(projected.company, "Google");
  assert.deepEqual(projected.rounds, ["DSA"]);
  assert.deepEqual(projected.tips, []);
  assert.equal(projected.uid, undefined);
  assert.equal(projected.reporterId, undefined);
  assert.equal(Object.prototype.hasOwnProperty.call(projected, "uid"), false);
});

test("cohort normalizers bound lengths and trim", () => {
  assert.equal(normalizeCollegeId(123), "");
  assert.equal(normalizeCollegeId("  IIT Delhi "), "IIT Delhi");
  assert.equal(normalizeCollegeId("c".repeat(500)).length, 120);
  assert.equal(normalizeTier("   "), undefined);
  assert.equal(normalizeTier(" tier_1 "), "tier_1");
  assert.equal(normalizeSemester(undefined), undefined);

  const projected = projectCohort("id", { collegeId: "IIT Delhi" });
  assert.equal(projected.placedCount, 0);
  assert.equal(projected.totalStudents, 0);
  assert.equal(projected.averagePackage, null);
});
