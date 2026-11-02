import { mkdir, writeFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import {
  GREENHOUSE_COMPANIES,
  LEVER_COMPANIES,
  getCuratedProgrammes,
  getDevfolioHackathons,
  getDevpostHackathons,
  getGreenhouseJobs,
  getLeverJobs,
  getUnstopInternships,
} from "./sources.mjs";
import { dedupe, deriveSkills, normalizeUrl } from "./earlyCareer.mjs";

const HERE = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(HERE, "../..");
const FEED_PATH = resolve(ROOT, "feed/opportunities.json");
const META_PATH = resolve(ROOT, "feed/meta.json");
const SNAPSHOT_PATH = resolve(ROOT, "app/src/main/assets/opportunities_seed.json");

const args = new Set(process.argv.slice(2));
const VERIFY = !args.has("--no-verify");
const ALLOW_SHORT = args.has("--allow-short");
const MIN_INTERNSHIPS = 12;
const MIN_HACKATHONS = 3;

function log(message) {
  process.stdout.write(`${message}\n`);
}

async function mapLimit(items, limit, worker) {
  const results = new Array(items.length);
  let cursor = 0;
  async function run() {
    while (cursor < items.length) {
      const index = cursor++;
      results[index] = await worker(items[index], index);
    }
  }
  await Promise.all(Array.from({ length: Math.min(limit, items.length) }, run));
  return results;
}

function cleanString(value, fallback = "") {
  if (typeof value !== "string") return fallback;
  return value.replace(/\s+/g, " ").trim() || fallback;
}

function validHttpUrl(value) {
  if (!value || typeof value !== "string") return false;
  try {
    const url = new URL(value);
    return url.protocol === "https:" || url.protocol === "http:";
  } catch {
    return false;
  }
}

function dateOnly(value) {
  if (!value) return null;
  if (typeof value === "string" && /^\d{4}-\d{2}-\d{2}$/.test(value)) return value;
  const parsed = Date.parse(value);
  if (!Number.isFinite(parsed)) return null;
  return new Date(parsed).toISOString().slice(0, 10);
}

function isoOrNull(value) {
  if (!value) return null;
  const parsed = Date.parse(value);
  if (!Number.isFinite(parsed)) return null;
  return new Date(parsed).toISOString();
}

function isFutureOrRolling(deadline) {
  if (!deadline) return true;
  return Date.parse(`${deadline}T23:59:59Z`) >= Date.now();
}

function sortInternships(a, b) {
  const aDeadline = a.deadline ? Date.parse(`${a.deadline}T23:59:59Z`) : Number.POSITIVE_INFINITY;
  const bDeadline = b.deadline ? Date.parse(`${b.deadline}T23:59:59Z`) : Number.POSITIVE_INFINITY;
  if (aDeadline !== bDeadline) return aDeadline - bDeadline;
  const aPosted = a.postedAt ? Date.parse(a.postedAt) : 0;
  const bPosted = b.postedAt ? Date.parse(b.postedAt) : 0;
  if (aPosted !== bPosted) return bPosted - aPosted;
  return a.title.localeCompare(b.title);
}

function sortHackathons(a, b) {
  const aDeadline = a.registrationDeadline ? Date.parse(`${a.registrationDeadline}T23:59:59Z`) : Number.POSITIVE_INFINITY;
  const bDeadline = b.registrationDeadline ? Date.parse(`${b.registrationDeadline}T23:59:59Z`) : Number.POSITIVE_INFINITY;
  if (aDeadline !== bDeadline) return aDeadline - bDeadline;
  return a.title.localeCompare(b.title);
}

function normalizeInternship(item, verifiedAt) {
  const title = cleanString(item.title);
  const company = cleanString(item.company);
  const location = cleanString(item.location, "Location not listed");
  const applyUrl = normalizeUrl(item.applyUrl || "");
  if (!title || !company || !validHttpUrl(applyUrl)) return null;
  const tags = [...new Set((item.tags || []).map((tag) => cleanString(tag)).filter(Boolean))].slice(0, 8);
  const description = cleanString(item.description, `${company} is hiring for ${title}. See the official posting for eligibility and details.`);
  // A card shows ~900 characters; the posting is longer. Requirements listed
  // below that cut still name real skills, so they are read from the full text
  // and never guessed.
  const skillText = cleanString(item.skillText, description);
  // Where the organiser typed a required-skills list, that is the strongest
  // evidence and fills the cap before anything inferred from prose.
  const stated = deriveSkills(`${title} ${cleanString(item.requiredSkillsText)}`, 4);
  const inferred = deriveSkills(`${title} ${skillText} ${tags.join(" ")}`);
  const skills = [...new Set([...stated, ...inferred])].slice(0, 6);
  return {
    id: cleanString(item.id, `role-${Buffer.from(applyUrl).toString("base64url").slice(0, 24)}`),
    kind: ["INTERNSHIP", "FELLOWSHIP", "SCHOLARSHIP", "PROGRAM"].includes(item.kind) ? item.kind : "INTERNSHIP",
    title,
    company,
    location,
    isRemote: Boolean(item.isRemote) || /\bremote\b/i.test(location),
    stipendLabel: cleanString(item.stipendLabel, "Stipend not listed"),
    duration: cleanString(item.duration, "See official page"),
    deadline: dateOnly(item.deadline),
    applyUrl,
    description,
    postedAt: isoOrNull(item.postedAt),
    sourceName: cleanString(item.sourceName, company),
    verifiedAt,
    tags,
    skills,
  };
}

function normalizeHackathon(item, verifiedAt) {
  const title = cleanString(item.title);
  const registrationUrl = normalizeUrl(item.registrationUrl || "");
  if (!title || !validHttpUrl(registrationUrl)) return null;
  const registrationDeadline = dateOnly(item.registrationDeadline);
  const description = cleanString(item.description, `${title} is accepting registrations. See the official page for themes, prizes, and eligibility.`);
  const tags = [...new Set((item.tags || []).map((tag) => cleanString(tag)).filter(Boolean))].slice(0, 8);
  return {
    id: cleanString(item.id, `hack-${Buffer.from(registrationUrl).toString("base64url").slice(0, 24)}`),
    title,
    organizer: cleanString(item.organizer, "Community"),
    description,
    mode: item.mode === "OFFLINE" ? "OFFLINE" : "ONLINE",
    location: cleanString(item.location, item.mode === "OFFLINE" ? "India" : "Online"),
    startDate: dateOnly(item.startDate),
    endDate: dateOnly(item.endDate),
    registrationDeadline,
    prizePool: cleanString(item.prizePool, "Prize pool on official page"),
    registrationUrl,
    isLive: true,
    tags,
    skills: deriveSkills(`${title} ${description} ${tags.join(" ")}`),
    sourceName: cleanString(item.sourceName, "Community"),
    verifiedAt,
  };
}

async function verifyUrl(url) {
  try {
    const response = await fetch(url, {
      redirect: "follow",
      headers: { "user-agent": "SagittaFeedBot/1.0 (+https://github.com/humoge7502/Nexus)" },
      signal: AbortSignal.timeout(15_000),
    });
    await response.body?.cancel();
    return { status: response.status, finalUrl: response.url, ok: response.status < 400 };
  } catch (error) {
    return { status: 0, finalUrl: url, ok: false, error: error.message };
  }
}

async function verifyItems(items, label) {
  if (!VERIFY) return { kept: items, verified: items.length, dropped: 0 };
  let verified = 0;
  let dropped = 0;
  const checked = await mapLimit(items, 8, async (item) => {
    const result = await verifyUrl(item.applyUrl || item.registrationUrl);
    if (result.ok) {
      verified += 1;
      return { ...item, verifiedAt: new Date().toISOString() };
    }
    // 401/403/429 mean the page exists but blocks bots. Keep it, unverified.
    if ([401, 403, 405, 429].includes(result.status)) {
      return { ...item, verifiedAt: null };
    }
    dropped += 1;
    log(`  drop ${result.status || "ERR"} ${item.title} — ${item.applyUrl || item.registrationUrl}`);
    return null;
  });
  log(`${label}: ${verified} verified, ${dropped} dead links dropped, ${items.length - verified - dropped} bot-blocked kept unverified`);
  return { kept: checked.filter(Boolean), verified, dropped };
}

function dedupeStrict(internships) {
  const roleKeys = new Set();
  const output = [];
  for (const internship of internships) {
    const roleKey = `${internship.company.toLowerCase()}|${internship.title.toLowerCase().replace(/\s+/g, " ")}`;
    if (roleKeys.has(roleKey)) continue;
    roleKeys.add(roleKey);
    output.push(internship);
  }
  return output;
}

function capPerCompany(internships, limit = 3) {
  const counts = new Map();
  const output = [];
  for (const internship of internships) {
    if (internship.kind !== "INTERNSHIP") {
      output.push(internship);
      continue;
    }
    const key = internship.company.toLowerCase();
    const count = counts.get(key) || 0;
    if (count >= limit) continue;
    counts.set(key, count + 1);
    output.push(internship);
  }
  return output;
}

async function main() {
  const startedAt = new Date();
  log(`Sagitta feed refresh — ${startedAt.toISOString()} — verify=${VERIFY}`);

  // Unstop pages one at a time out of politeness; the boards and hackathon
  // sources run alongside it so the wait is one crawl, not two.
  const unstopPromise = getUnstopInternships();
  const jobs = await mapLimit(
    [
      ...GREENHOUSE_COMPANIES.map((company) => () => getGreenhouseJobs(company)),
      ...LEVER_COMPANIES.map((company) => () => getLeverJobs(company)),
    ],
    5,
    (run) => run(),
  );
  const devpost = await getDevpostHackathons();
  const devfolio = await getDevfolioHackathons();
  const curated = getCuratedProgrammes();
  const unstop = await unstopPromise;

  const sourceResults = [...jobs, unstop, devpost, devfolio, curated];
  const internshipItems = sourceResults.flatMap((result) => result.items.filter((item) => item.applyUrl));
  const hackathonItems = sourceResults.flatMap((result) => result.items.filter((item) => item.registrationUrl));

  log(`Collected ${internshipItems.length} internship candidates and ${hackathonItems.length} hackathon candidates`);

  const normalizedInternships = dedupeStrict(
    dedupe(internshipItems.map((item) => normalizeInternship(item, null)).filter(Boolean)),
  ).filter((item) => isFutureOrRolling(item.deadline));

  const normalizedHackathons = dedupe(
    hackathonItems.map((item) => normalizeHackathon(item, null)).filter(Boolean),
  ).filter((item) => isFutureOrRolling(item.registrationDeadline));

  const verifiedInternships = await verifyItems(normalizedInternships, "Internships");
  const verifiedHackathons = await verifyItems(normalizedHackathons, "Hackathons");

  const internships = capPerCompany(verifiedInternships.kept.sort(sortInternships));
  const hackathons = verifiedHackathons.kept.sort(sortHackathons);

  log(`Keeping ${internships.length} internships and ${hackathons.length} hackathons`);

  if (!ALLOW_SHORT && (internships.length < MIN_INTERNSHIPS || hackathons.length < MIN_HACKATHONS)) {
    throw new Error(
      `Feed below safety threshold (internships ${internships.length}/${MIN_INTERNSHIPS}, hackathons ${hackathons.length}/${MIN_HACKATHONS}). Refusing to overwrite the published feed. Run with --allow-short to override.`,
    );
  }

  const generatedAt = new Date().toISOString();
  const feed = {
    version: 2,
    generatedAt,
    counts: {
      internships: internships.length,
      hackathons: hackathons.length,
      verifiedInternships: verifiedInternships.verified,
      verifiedHackathons: verifiedHackathons.verified,
    },
    internships,
    hackathons,
  };

  const meta = {
    version: 2,
    generatedAt,
    durationMs: Date.now() - startedAt.getTime(),
    counts: feed.counts,
    verification: {
      enabled: VERIFY,
      deadLinksDropped: verifiedInternships.dropped + verifiedHackathons.dropped,
    },
    sources: sourceResults.map((result) => ({
      name: result.source,
      ok: result.ok,
      status: result.status ?? null,
      count: result.items.length,
    })),
  };

  const serialized = `${JSON.stringify(feed, null, 2)}\n`;
  await mkdir(dirname(FEED_PATH), { recursive: true });
  await writeFile(FEED_PATH, serialized, "utf8");
  await writeFile(META_PATH, `${JSON.stringify(meta, null, 2)}\n`, "utf8");
  await writeFile(SNAPSHOT_PATH, serialized, "utf8");

  log(`Wrote ${FEED_PATH}`);
  log(`Wrote ${META_PATH}`);
  log(`Wrote ${SNAPSHOT_PATH}`);
  log(
    `Done in ${(meta.durationMs / 1000).toFixed(1)}s — ${internships.length} internships, ${hackathons.length} hackathons`,
  );
}

main().catch((error) => {
  console.error(`Feed refresh failed: ${error.message}`);
  process.exitCode = 1;
});
