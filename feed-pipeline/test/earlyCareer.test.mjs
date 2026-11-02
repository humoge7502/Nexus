import test from "node:test";
import assert from "node:assert/strict";
import {
  dedupe,
  dedupeKey,
  deriveSkills,
  isEarlyCareerTitle,
  isEligibleLocation,
  isRollingOrFutureDeadline,
  normalizeUrl,
  parseDevpostEndDate,
  parseDevpostTimeLeft,
  stripHtml,
  truncate,
} from "../src/earlyCareer.mjs";
import { CURATED_PROGRAMMES, GREENHOUSE_COMPANIES, LEVER_COMPANIES } from "../src/sources.mjs";

test("isEarlyCareerTitle accepts genuine student roles", () => {
  for (const title of [
    "Software Engineer Intern",
    "Internship - Talent Acquisition",
    "Graduate Software Engineer",
    "Junior Data Analyst",
    "New Grad Backend Engineer",
    "Apprentice Developer",
    "Trainee Engineer",
  ]) {
    assert.equal(isEarlyCareerTitle(title), true, title);
  }
});

test("isEarlyCareerTitle rejects senior roles and false substrings", () => {
  for (const title of [
    "Senior Software Engineer Intern",
    "Engineering Manager",
    "Principal Architect",
    "Director of Engineering",
    "Internal Auditor",
    "International Sales Lead",
    "Staff Software Engineer",
  ]) {
    assert.equal(isEarlyCareerTitle(title), false, title);
  }
});

test("isEligibleLocation accepts India and genuinely global remote", () => {
  for (const location of [
    "Bengaluru, India",
    "Hyderabad",
    "Remote - India",
    "Remote / Worldwide",
    "Anywhere",
    "APAC",
  ]) {
    assert.equal(isEligibleLocation(location), true, location);
  }
});

test("isEligibleLocation rejects restricted remote regions and unknown places", () => {
  for (const location of [
    "Remote - US",
    "San Francisco, CA",
    "London, United Kingdom",
    "EMEA",
    "Singapore",
    "",
    null,
  ]) {
    assert.equal(isEligibleLocation(location), false, String(location));
  }
});

test("stripHtml removes markup and decodes entities", () => {
  assert.equal(
    stripHtml("<p>Build <strong>real</strong> things</p><br/>Apply &amp; ship&nbsp;today"),
    "Build real things Apply & ship today",
  );
  assert.equal(stripHtml(undefined), "");
});

test("truncate keeps words intact", () => {
  assert.equal(truncate("one two three", 100), "one two three");
  const value = truncate("one two three four five", 14);
  assert.ok(value.endsWith("…"));
  assert.ok(value.length <= 16);
  assert.ok(!value.includes("fou…"));
});

test("normalizeUrl drops tracking parameters and fragments", () => {
  assert.equal(
    normalizeUrl("https://example.com/jobs/1?utm_source=x&ref=y&gh_src=z&team=core#apply"),
    "https://example.com/jobs/1?team=core",
  );
  assert.equal(normalizeUrl("not a url"), "");
});

test("dedupe keeps the first item per normalized URL", () => {
  const items = [
    { applyUrl: "https://example.com/jobs/1?utm_source=a", title: "A" },
    { applyUrl: "https://example.com/jobs/1?utm_source=b", title: "B" },
    { applyUrl: "https://example.com/jobs/2", title: "C" },
  ];
  assert.deepEqual(dedupe(items).map((item) => item.title), ["A", "C"]);
  assert.equal(dedupeKey({ company: "Acme", title: "Intern" }), "role:acme|intern");
});

test("parseDevpostEndDate understands the ranges Devpost actually returns", () => {
  const now = new Date("2026-11-01T00:00:00Z");
  assert.equal(
    new Date(parseDevpostEndDate("Oct 01 - Nov 12, 2026", now)).toISOString().slice(0, 10),
    "2026-11-12",
  );
  assert.equal(
    new Date(parseDevpostEndDate("Dec 20 - Jan 05", now)).toISOString().slice(0, 10),
    "2027-01-05",
  );
  // Same-month ranges drop the repeated month name; the trailing half has to
  // borrow it from the front or the listing disappears with a null.
  assert.equal(
    new Date(parseDevpostEndDate("Nov 01 - 12, 2026", now)).toISOString().slice(0, 10),
    "2026-11-12",
  );
  assert.equal(
    new Date(parseDevpostEndDate("Jun 24 – Nov 15, 2026", now)).toISOString().slice(0, 10),
    "2026-11-15",
  );
  assert.equal(parseDevpostEndDate(""), null);
});

test("parseDevpostEndDate refuses to invent a future deadline for a closed event", () => {
  const now = new Date("2026-11-03T00:00:00Z");
  assert.equal(parseDevpostEndDate("Oct 23 - Nov 02, 2026", now), null);
  // A range printed without a year is next year's edition, not a stale one:
  // this endpoint only lists events Devpost itself still calls open.
  assert.equal(
    new Date(parseDevpostEndDate("Apr 04 - May 06", now)).toISOString().slice(0, 10),
    "2027-05-06",
  );
});

test("parseDevpostTimeLeft reads the countdown on the live listings", () => {
  const now = new Date("2026-11-02T12:00:00Z");
  const day = (iso) => new Date(parseDevpostTimeLeft(iso, now)).toISOString().slice(0, 10);
  assert.equal(day("10 days left"), "2026-11-12");
  assert.equal(day("about 1 month left"), "2026-12-02");
  assert.equal(day("about 2 months left"), "2027-01-01");
  assert.equal(day("16 days left"), "2026-11-18");
  // One hour out is a real deadline, not a rounding artefact.
  assert.ok(parseDevpostTimeLeft("about 1 hour left", now) > now.getTime());
  assert.equal(parseDevpostTimeLeft("Submissions closed", now), null);
  assert.equal(parseDevpostTimeLeft(null, now), null);
});

test("isRollingOrFutureDeadline treats missing deadlines as rolling", () => {
  const now = Date.parse("2026-11-01T00:00:00Z");
  assert.equal(isRollingOrFutureDeadline(null, now), true);
  assert.equal(isRollingOrFutureDeadline(undefined, now), true);
  assert.equal(isRollingOrFutureDeadline(now + 1000, now), true);
  assert.equal(isRollingOrFutureDeadline(now - 1000, now), false);
});

test("curated programmes are unique, https, and carry provenance", () => {
  const ids = new Set();
  for (const programme of CURATED_PROGRAMMES) {
    assert.ok(programme.id, "missing id");
    assert.ok(!ids.has(programme.id), `duplicate id ${programme.id}`);
    ids.add(programme.id);
    assert.match(programme.applyUrl, /^https:\/\//, programme.id);
    assert.ok(programme.description.length > 40, `${programme.id} description too short`);
    assert.ok(programme.tags.length > 0, `${programme.id} needs tags`);
    assert.ok(programme.sourceName, `${programme.id} needs a source`);
    assert.ok(
      ["INTERNSHIP", "FELLOWSHIP", "SCHOLARSHIP", "PROGRAM"].includes(programme.kind),
      `${programme.id} bad kind`,
    );
  }
});

test("company board definitions are unique", () => {
  const tokens = [...GREENHOUSE_COMPANIES, ...LEVER_COMPANIES].map((company) => company.token);
  assert.equal(new Set(tokens).size, tokens.length);
});

test("deriveSkills claims only skills the text actually names", () => {
  const skills = deriveSkills(
    "Backend Intern - you will build services in Go and Python with PostgreSQL and Docker",
  );
  assert.deepEqual(skills.sort(), ["DevOps", "Python", "SQL"].sort());
});

test("deriveSkills does not invent a cloud skill from prose about services", () => {
  assert.deepEqual(deriveSkills("Build distributed services and own the deployment pipeline"), []);
  assert.deepEqual(deriveSkills("Work across AWS, GCP and Kubernetes"), ["Cloud Computing"]);
});

test("deriveSkills does not confuse Java with JavaScript", () => {
  assert.deepEqual(deriveSkills("Frontend Intern using JavaScript"), ["JavaScript"]);
  assert.deepEqual(deriveSkills("Core Java maintainer for the JVM runtime team"), ["Java"]);
});

test("deriveSkills returns nothing when the posting names no known skill", () => {
  assert.deepEqual(deriveSkills("Talent Acquisition Intern - HR onboarding, stakeholder management"), []);
});

test("deriveSkills caps the list", () => {
  const many = deriveSkills("Python Java Kotlin C++ SQL React Android Cloud DevOps");
  assert.equal(many.length, 6);
});

test("deriveSkills stays inside the vocabulary onboarding offers", () => {
  // Mirrors OnboardingViewModel.skillOptions. A skill outside this set can never
  // be selected by a student, so it would silently cap their match score.
  const ONBOARDING_SKILLS = new Set([
    "Python", "Java", "JavaScript", "Kotlin", "C++", "SQL", "React", "Android",
    "Machine Learning", "Data Science", "Cloud Computing", "DevOps", "UI/UX", "Blockchain",
  ]);
  for (const programme of CURATED_PROGRAMMES) {
    for (const skill of deriveSkills(`${programme.title} ${programme.description} ${programme.tags.join(" ")}`)) {
      assert.ok(ONBOARDING_SKILLS.has(skill), `skill not selectable by a student: ${skill}`);
    }
  }
});
