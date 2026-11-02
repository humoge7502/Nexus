const EARLY_TOKENS =
  /\b(intern|interns|internship|internships|trainee|apprentice|new grad|new graduate|graduate|junior|entry[- ]level|fresher)\b/i;

const NON_STUDENT_SENIORITY =
  /\b(senior|staff|principal|lead|manager|director|head|vp|chief|architect|president|officer)\b/i;

const INDIA_LOCATION =
  /(india|bengaluru|bangalore|hyderabad|mumbai|pune|delhi|gurugram|gurgaon|noida|chennai|kolkata|ahmedabad|jaipur|indore|kochi|coimbatore|trivandrum|chandigarh|bhubaneswar|mysore|nagpur)/i;

const GLOBAL_LOCATION = /(remote|anywhere|worldwide|global|apac|asia)/i;

const RESTRICTED_REGION =
  /(united states|u\.s\.|u\.s\.a|\busa?\b|canada|united kingdom|\buk\b|emea|europe|europa|latam|latin america|australia|germany|france|spain|italy|netherlands|poland|sweden|ireland|singapore|japan|korea|china|brazil|mexico|israel|uae|dubai)/i;

export function isEarlyCareerTitle(title) {
  if (!title || typeof title !== "string") return false;
  return EARLY_TOKENS.test(title) && !NON_STUDENT_SENIORITY.test(title);
}

/**
 * A role is useful to an India-based student when it is located in India, or
 * when it is genuinely location-agnostic. "Remote - US" is not remote for us.
 */
export function isEligibleLocation(location) {
  if (!location || typeof location !== "string") return false;
  if (INDIA_LOCATION.test(location)) return true;
  if (!GLOBAL_LOCATION.test(location)) return false;
  return !RESTRICTED_REGION.test(location);
}

export function stripHtml(value) {
  if (!value || typeof value !== "string") return "";
  return value
    .replace(/<script[\s\S]*?<\/script>/gi, " ")
    .replace(/<style[\s\S]*?<\/style>/gi, " ")
    .replace(/<\/(p|div|li|h[1-6]|br)>/gi, " ")
    .replace(/<br\s*\/?>/gi, " ")
    .replace(/<[^>]+>/g, " ")
    .replace(/&nbsp;/gi, " ")
    .replace(/&amp;/gi, "&")
    .replace(/&lt;/gi, "<")
    .replace(/&gt;/gi, ">")
    .replace(/&quot;/gi, '"')
    .replace(/&#39;/gi, "'")
    .replace(/\s+/g, " ")
    .trim();
}

export function truncate(value, max = 900) {
  const text = (value || "").trim();
  if (text.length <= max) return text;
  const cut = text.slice(0, max);
  const lastSpace = cut.lastIndexOf(" ");
  return `${cut.slice(0, lastSpace > 0 ? lastSpace : max)}…`;
}

export function normalizeUrl(url) {
  if (!url || typeof url !== "string") return "";
  try {
    const parsed = new URL(url);
    parsed.hash = "";
    for (const key of [...parsed.searchParams.keys()]) {
      if (/^(utm_|ref|source|gh_src|lever-source)/i.test(key)) {
        parsed.searchParams.delete(key);
      }
    }
    return parsed.toString();
  } catch {
    return "";
  }
}

export function dedupeKey(item) {
  const url = normalizeUrl(item.applyUrl || "");
  if (url) return `url:${url.toLowerCase()}`;
  return `role:${(item.company || "").toLowerCase().trim()}|${(item.title || "")
    .toLowerCase()
    .replace(/\s+/g, " ")
    .trim()}`;
}

export function dedupe(items) {
  const seen = new Set();
  const output = [];
  for (const item of items) {
    const key = dedupeKey(item);
    if (seen.has(key)) continue;
    seen.add(key);
    output.push(item);
  }
  return output;
}

const MONTHS = {
  jan: 0,
  feb: 1,
  mar: 2,
  apr: 3,
  may: 4,
  jun: 5,
  jul: 6,
  aug: 7,
  sep: 8,
  oct: 9,
  nov: 10,
  dec: 11,
};

export function parseDevpostEndDate(range, now = new Date()) {
  if (!range || typeof range !== "string") return null;
  const end = range.split(/\s*[-–—]\s*/).pop();
  if (!end) return null;
  const parts = end.match(/([A-Za-z]{3,})\s+(\d{1,2})(?:,\s*(\d{4}))?/);
  if (!parts) return null;
  const month = MONTHS[parts[1].slice(0, 3).toLowerCase()];
  const day = Number(parts[2]);
  if (month === undefined || day < 1 || day > 31) return null;
  const explicitYear = parts[3] ? Number(parts[3]) : null;
  const year = explicitYear ?? now.getUTCFullYear();
  let timestamp = Date.UTC(year, month, day, 23, 59, 59);
  if (!explicitYear && timestamp < now.getTime() - 1000 * 60 * 60 * 24 * 180) {
    timestamp = Date.UTC(year + 1, month, day, 23, 59, 59);
  }
  return timestamp;
}

/**
 * Skills the app can score a student against, named exactly the way onboarding
 * names them, so the two vocabularies meet without a translation layer.
 *
 * A skill is only claimed when the posting itself names it. Under-claiming is
 * honest; guessing is not, because a fabricated match score is the one number
 * on the card a student cannot verify.
 */
const SKILL_RULES = [
  { skill: "Python", pattern: /\bpython\b/i },
  { skill: "Java", pattern: /\bjava\b/i },
  { skill: "JavaScript", pattern: /\bjavascript\b|\btypescript\b|\bnode\.?js\b/i },
  { skill: "Kotlin", pattern: /\bkotlin\b/i },
  { skill: "C++", pattern: /\bc\+\+/i },
  { skill: "SQL", pattern: /\bsql\b|postgres|mysql|sqlite|mariadb|redshift|databricks/i },
  { skill: "React", pattern: /\breact\b|\breactjs\b|\breact\.js\b/i },
  { skill: "Android", pattern: /\bandroid\b/i },
  { skill: "Machine Learning", pattern: /\bmachine learning\b|\bdeep learning\b|\bpytorch\b|\btensorflow\b|\bartificial intelligence\b|\bai\/ml\b/i },
  { skill: "Data Science", pattern: /\bdata science\b|\bdata analysis\b|\bdata analytics\b|\bstatistics\b/i },
  { skill: "Cloud Computing", pattern: /\baws\b|\bgcp\b|\bazure\b|\bkubernetes\b|\bcloud\b/i },
  { skill: "DevOps", pattern: /\bdevops\b|\bci\/cd\b|\bterraform\b|\bdocker\b/i },
  { skill: "UI/UX", pattern: /\bui\b|\bux\b|\bfigma\b|\bwireframe\b|\bprototyp/i },
  { skill: "Blockchain", pattern: /\bblockchain\b|\bsolidity\b|\bweb3\b|\bbitcoin\b/i },
];

export function deriveSkills(text, max = 6) {
  if (!text || typeof text !== "string") return [];
  return SKILL_RULES.filter((rule) => rule.pattern.test(text))
    .map((rule) => rule.skill)
    .slice(0, max);
}

export function isRollingOrFutureDeadline(deadline, now = Date.now()) {
  if (deadline === null || deadline === undefined) return true;
  return Number(deadline) >= now;
}
