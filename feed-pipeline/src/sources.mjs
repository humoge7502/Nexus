import {
  stripHtml,
  truncate,
  isEarlyCareerTitle,
  isEligibleLocation,
  parseDevpostEndDate,
} from "./earlyCareer.mjs";

const UA = "SagittaFeedBot/1.0 (+https://github.com/humoge7502/Nexus)";
const TIMEOUT_MS = 20_000;

export async function fetchText(url, { timeout = TIMEOUT_MS } = {}) {
  try {
    const response = await fetch(url, {
      redirect: "follow",
      headers: { "user-agent": UA, accept: "application/json, text/html;q=0.9, */*;q=0.8" },
      signal: AbortSignal.timeout(timeout),
    });
    if (!response.ok) return { ok: false, status: response.status, url: response.url, text: "" };
    return { ok: true, status: response.status, url: response.url, text: await response.text() };
  } catch (error) {
    return { ok: false, status: 0, url, text: "", error: error.message };
  }
}

async function fetchJson(url) {
  const result = await fetchText(url);
  if (!result.ok) return { ...result, data: null };
  try {
    return { ...result, data: JSON.parse(result.text) };
  } catch {
    return { ...result, data: null, error: "invalid json" };
  }
}

export const GREENHOUSE_COMPANIES = [
  { token: "stripe", name: "Stripe" },
  { token: "databricks", name: "Databricks" },
  { token: "coinbase", name: "Coinbase" },
  { token: "figma", name: "Figma" },
  { token: "cloudflare", name: "Cloudflare" },
  { token: "mongodb", name: "MongoDB" },
  { token: "duolingo", name: "Duolingo" },
  { token: "pinterest", name: "Pinterest" },
  { token: "robinhood", name: "Robinhood" },
  { token: "affirm", name: "Affirm" },
  { token: "vercel", name: "Vercel" },
  { token: "datadog", name: "Datadog" },
  { token: "gitlab", name: "GitLab" },
  { token: "elastic", name: "Elastic" },
  
  { token: "dropbox", name: "Dropbox" },
  { token: "twilio", name: "Twilio" },
  
  
  
  
  { token: "reddit", name: "Reddit" },
  
  { token: "instacart", name: "Instacart" },
  { token: "brex", name: "Brex" },
  { token: "ramp", name: "Ramp" },
  
  { token: "netlify", name: "Netlify" },
  
  
  { token: "airtable", name: "Airtable" },
  { token: "discord", name: "Discord" },
  { token: "asana", name: "Asana" },
  
  { token: "groww", name: "Groww" },
  { token: "slice", name: "Slice" },
  { token: "porter", name: "Porter" },
  { token: "highradius", name: "HighRadius" },
];

export const LEVER_COMPANIES = [
  { token: "paytm", name: "Paytm" },
  { token: "meesho", name: "Meesho" },
  { token: "cred", name: "CRED" },
  { token: "zeta", name: "Zeta" },
  { token: "palantir", name: "Palantir" },
  
  { token: "spotify", name: "Spotify" },
];

const TAG_RULES = [
  { tag: "AI/ML", pattern: /\b(ai|ml|machine learning|deep learning|data science|nlp|computer vision|llm|pytorch|tensorflow)\b/i },
  { tag: "Web", pattern: /\b(web|react|javascript|typescript|frontend|backend|node|html|css|next\.js)\b/i },
  { tag: "Mobile", pattern: /\b(mobile|android|ios|kotlin|flutter|swift|react native)\b/i },
  { tag: "Cloud", pattern: /\b(cloud|aws|gcp|azure|devops|docker|kubernetes|serverless|infrastructure)\b/i },
  { tag: "Data", pattern: /\b(data engineer|analytics|sql|spark|warehouse|etl)\b/i },
  { tag: "Security", pattern: /\b(security|cybersecurity|appsec|infosec)\b/i },
  { tag: "Design", pattern: /\b(design|ux|ui|figma|product design)\b/i },
];

function deriveTags(text) {
  return TAG_RULES.filter((rule) => rule.pattern.test(text)).map((rule) => rule.tag);
}

function locationTag(location) {
  if (!location) return [];
  if (/india|bengaluru|bangalore|hyderabad|mumbai|pune|delhi|gurugram|gurgaon|noida|chennai|kolkata|ahmedabad|jaipur|indore|kochi/i.test(location)) {
    return ["India"];
  }
  if (/remote|anywhere|worldwide|global/i.test(location)) return ["Remote"];
  return [];
}

function isRemoteLocation(location) {
  return /\b(remote|anywhere|worldwide|distributed)\b/i.test(location || "");
}

export async function getGreenhouseJobs(company) {
  const url = `https://boards-api.greenhouse.io/v1/boards/${company.token}/jobs?content=true`;
  const result = await fetchJson(url);
  if (!Array.isArray(result.data?.jobs)) {
    return { source: company.name, ok: false, status: result.status, items: [] };
  }
  const items = [];
  for (const job of result.data.jobs) {
    const location = job.location?.name || "";
    if (!isEarlyCareerTitle(job.title)) continue;
    if (!isEligibleLocation(location)) continue;
    const fullText = stripHtml(job.content);
    const description = truncate(fullText, 900);
    const departments = (job.departments || []).map((department) => department.name).filter(Boolean);
    items.push({
      id: `gh-${company.token}-${job.id}`,
      kind: "INTERNSHIP",
      title: job.title.trim(),
      company: company.name,
      location,
      isRemote: isRemoteLocation(location),
      stipendLabel: "Stipend not listed",
      duration: "See official page",
      deadline: null,
      applyUrl: job.absolute_url,
      description: description || `${company.name} is hiring for ${job.title}. See the official posting for eligibility and compensation.`,
      skillText: fullText,
      postedAt: job.updated_at || null,
      sourceName: `${company.name} careers (Greenhouse)`,
      tags: [...new Set([...departments, ...deriveTags(`${job.title} ${fullText}`), ...locationTag(location)])],
    });
  }
  return { source: company.name, ok: true, status: result.status, items };
}

export async function getLeverJobs(company) {
  const url = `https://api.lever.co/v0/postings/${company.token}?mode=json`;
  const result = await fetchJson(url);
  if (!Array.isArray(result.data)) {
    return { source: company.name, ok: false, status: result.status, items: [] };
  }
  const items = [];
  for (const posting of result.data) {
    const location = posting.categories?.location || "";
    if (!isEarlyCareerTitle(posting.text)) continue;
    if (!isEligibleLocation(location)) continue;
    const fullText =
      posting.descriptionPlain || stripHtml(posting.description) || stripHtml(posting.descriptionBody);
    const description = truncate(fullText, 900);
    items.push({
      id: `lv-${company.token}-${posting.id}`,
      kind: "INTERNSHIP",
      title: posting.text.trim(),
      company: company.name,
      location,
      isRemote: isRemoteLocation(location),
      stipendLabel: "Stipend not listed",
      duration: posting.categories?.commitment || "See official page",
      deadline: null,
      applyUrl: posting.hostedUrl,
      description: description || `${company.name} is hiring for ${posting.text}. See the official posting for eligibility and compensation.`,
      skillText: fullText,
      postedAt: posting.createdAt ? new Date(posting.createdAt).toISOString() : null,
      sourceName: `${company.name} careers (Lever)`,
      tags: [...new Set([posting.categories?.team, ...deriveTags(`${posting.text} ${fullText}`), ...locationTag(location)].filter(Boolean))],
    });
  }
  return { source: company.name, ok: true, status: result.status, items };
}

export async function getDevpostHackathons() {
  const result = await fetchJson("https://devpost.com/api/hackathons?status[]=open&limit=100");
  if (!Array.isArray(result.data?.hackathons)) {
    return { source: "Devpost", ok: false, status: result.status, items: [] };
  }
  const now = Date.now();
  const items = [];
  for (const hackathon of result.data.hackathons) {
    if (hackathon.open_state !== "open") continue;
    const locationLabel = hackathon.displayed_location?.location || "";
    if (locationLabel && !/online/i.test(locationLabel) && !isEligibleLocation(locationLabel)) continue;
    const end = parseDevpostEndDate(hackathon.submission_period_dates);
    if (!end || end < now) continue;
    const themes = (hackathon.themes || []).map((theme) => theme.name).filter(Boolean);
    const prize = stripHtml(hackathon.prize_amount || "");
    items.push({
      id: `dp-${hackathon.id}`,
      title: hackathon.title?.trim() || "Open hackathon",
      organizer: hackathon.organization_name?.trim() || "Devpost",
      description: `Open hackathon on Devpost${themes.length ? ` focused on ${themes.join(", ")}` : ""}. ${hackathon.time_left_to_submission ? `Submissions close in ${hackathon.time_left_to_submission}.` : ""}`.trim(),
      mode: /online/i.test(locationLabel) || !locationLabel ? "ONLINE" : "OFFLINE",
      location: locationLabel || "Online",
      startDate: null,
      endDate: new Date(end).toISOString().slice(0, 10),
      registrationDeadline: new Date(end).toISOString().slice(0, 10),
      prizePool: prize || "Prize pool on official page",
      registrationUrl: hackathon.url,
      isLive: true,
      tags: [...new Set([...themes, "Hackathon", locationLabel || "Online"])],
      sourceName: "Devpost",
    });
  }
  return { source: "Devpost", ok: true, status: result.status, items };
}

export async function getDevfolioHackathons() {
  const result = await fetchText("https://devfolio.co/hackathons");
  if (!result.ok) {
    return { source: "Devfolio", ok: false, status: result.status, items: [] };
  }
  const match = result.text.match(/__NEXT_DATA__[^>]*>([\s\S]*?)<\/script>/);
  if (!match) {
    return { source: "Devfolio", ok: false, status: result.status, items: [] };
  }
  let payload;
  try {
    payload = JSON.parse(match[1]);
  } catch {
    return { source: "Devfolio", ok: false, status: result.status, items: [] };
  }
  const queries = payload?.props?.pageProps?.dehydratedState?.queries || [];
  const hackathons = queries.find((query) => Array.isArray(query?.state?.data?.open_hackathons))
    ?.state.data.open_hackathons || [];
  const now = Date.now();
  const items = [];
  for (const hackathon of hackathons) {
    const registrationEnd = hackathon.settings?.reg_ends_at;
    const endsAt = hackathon.ends_at;
    const close = registrationEnd || endsAt;
    if (!close) continue;
    const closeTime = Date.parse(close);
    if (!Number.isFinite(closeTime) || closeTime < now) continue;
    const themes = (hackathon.themes || []).map((theme) => theme.theme?.name).filter((name) => name && name !== "No Restrictions");
    const isOnline = Boolean(hackathon.is_online);
    const site = hackathon.settings?.site || `https://${hackathon.slug}.devfolio.co/`;
    items.push({
      id: `df-${hackathon.uuid || hackathon.slug}`,
      title: hackathon.name?.trim() || "Open hackathon",
      organizer: "Devfolio community",
      description: `${isOnline ? "Online" : "In-person"} hackathon listed on Devfolio${themes.length ? `, themed around ${themes.join(", ")}` : ""}. ${hackathon.participants_count ? `${hackathon.participants_count.toLocaleString("en-IN")} students have registered.` : ""} Registration closes ${new Date(closeTime).toISOString().slice(0, 10)}.`.trim(),
      mode: isOnline ? "ONLINE" : "OFFLINE",
      location: isOnline ? "Online" : "India",
      startDate: hackathon.starts_at ? new Date(hackathon.starts_at).toISOString().slice(0, 10) : null,
      endDate: endsAt ? new Date(endsAt).toISOString().slice(0, 10) : null,
      registrationDeadline: new Date(closeTime).toISOString().slice(0, 10),
      prizePool: "Prize pool on official page",
      registrationUrl: site,
      isLive: true,
      tags: [...new Set([...themes, "Hackathon", isOnline ? "Online" : "India"])],
      sourceName: "Devfolio",
    });
  }
  return { source: "Devfolio", ok: true, status: result.status, items };
}

/**
 * Evergreen official programmes. Deadlines are intentionally null: these run on
 * annual or rolling cycles and inventing a date would be worse than showing none.
 */
export const CURATED_PROGRAMMES = [
  {
    id: "program-gsoc",
    kind: "INTERNSHIP",
    title: "Google Summer of Code",
    company: "Google Open Source",
    location: "Remote / Global",
    isRemote: true,
    stipendLabel: "Paid stipend (varies by project)",
    duration: "12–16 weeks",
    deadline: null,
    applyUrl: "https://summerofcode.withgoogle.com/",
    description:
      "Google's annual open-source program that pairs students with mentor organizations for a paid summer project. The organization list is published in February and applications typically open in March.",
    postedAt: null,
    sourceName: "Google Summer of Code",
    tags: ["Open Source", "Paid", "Global", "Mentorship"],
  },
  {
    id: "program-outreachy",
    kind: "INTERNSHIP",
    title: "Outreachy",
    company: "Software Freedom Conservancy",
    location: "Remote / Global",
    isRemote: true,
    stipendLabel: "USD 7,000 stipend",
    duration: "3 months",
    deadline: null,
    applyUrl: "https://www.outreachy.org/",
    description:
      "Paid, remote open-source internships for people subject to systemic bias and underrepresentation in tech. The program runs twice a year with a mentorship contribution period before the internship.",
    postedAt: null,
    sourceName: "Outreachy",
    tags: ["Open Source", "Paid", "Remote", "Diversity"],
  },
  {
    id: "program-lfx",
    kind: "INTERNSHIP",
    title: "LFX Mentorship",
    company: "The Linux Foundation",
    location: "Remote / Global",
    isRemote: true,
    stipendLabel: "Paid stipend (varies by term)",
    duration: "3–6 months",
    deadline: null,
    applyUrl: "https://lfx.linuxfoundation.org/tools/mentorship/",
    description:
      "The Linux Foundation's mentorship program with paid, remote projects across cloud, security, networking, and open-source infrastructure. New terms open several times a year.",
    postedAt: null,
    sourceName: "LFX Mentorship",
    tags: ["Open Source", "Paid", "Cloud", "Security", "Mentorship"],
  },
  {
    id: "program-mlh-fellowship",
    kind: "FELLOWSHIP",
    title: "MLH Fellowship",
    company: "Major League Hacking",
    location: "Remote / Global",
    isRemote: true,
    stipendLabel: "Paid stipend",
    duration: "12 weeks",
    deadline: null,
    applyUrl: "https://fellowship.mlh.io/",
    description:
      "A 12-week remote fellowship where students contribute to open-source projects in small pods with mentor support. Tracks include open source, production engineering, and site reliability.",
    postedAt: null,
    sourceName: "MLH Fellowship",
    tags: ["Open Source", "Fellowship", "Remote", "Mentorship"],
  },
  {
    id: "program-c4gt",
    kind: "INTERNSHIP",
    title: "Code for GovTech (C4GT)",
    company: "Samagra / Code for GovTech",
    location: "Remote / India",
    isRemote: true,
    stipendLabel: "Paid stipend",
    duration: "3 months",
    deadline: null,
    applyUrl: "https://www.codeforgovtech.in/",
    description:
      "An Indian open-source mentoring program that places contributors on public-good projects with government and nonprofit partners. Selected contributors work with maintainers on live GovTech products.",
    postedAt: null,
    sourceName: "Code for GovTech",
    tags: ["Open Source", "India", "GovTech", "Mentorship"],
  },
  {
    id: "program-gssoc",
    kind: "PROGRAM",
    title: "GirlScript Summer of Code",
    company: "GirlScript Foundation",
    location: "Remote / India",
    isRemote: true,
    stipendLabel: "Rewards and certificates",
    duration: "3 months",
    deadline: null,
    applyUrl: "https://gssoc.girlscript.org/",
    description:
      "An open-source contribution program for beginners in India, with mentors, project listings, and rewards for top contributors. Participants make pull requests to real projects over the summer.",
    postedAt: null,
    sourceName: "GirlScript Summer of Code",
    tags: ["Open Source", "India", "Beginners", "Mentorship"],
  },
  {
    id: "program-summer-of-bitcoin",
    kind: "INTERNSHIP",
    title: "Summer of Bitcoin",
    company: "Summer of Bitcoin",
    location: "Remote / Global",
    isRemote: true,
    stipendLabel: "Paid stipend",
    duration: "12 weeks",
    deadline: null,
    applyUrl: "https://www.summerofbitcoin.org/",
    description:
      "A global program that pairs students with Bitcoin open-source projects for a paid summer of remote mentorship and contribution. Applicants complete a short online course before project selection.",
    postedAt: null,
    sourceName: "Summer of Bitcoin",
    tags: ["Open Source", "Bitcoin", "Paid", "Global"],
  },
  {
    id: "program-season-of-kde",
    kind: "PROGRAM",
    title: "Season of KDE",
    company: "KDE Community",
    location: "Remote / Global",
    isRemote: true,
    stipendLabel: "No stipend; mentorship and swag",
    duration: "12 weeks",
    deadline: null,
    applyUrl: "https://mentorship.kde.org/sok/",
    description:
      "KDE's annual mentorship program for students and newcomers to contribute to KDE open-source projects with a dedicated mentor. Projects span desktop apps, frameworks, and infrastructure.",
    postedAt: null,
    sourceName: "KDE",
    tags: ["Open Source", "KDE", "Mentorship", "Global"],
  },
  {
    id: "program-aicte-internship",
    kind: "INTERNSHIP",
    title: "AICTE Internship Portal",
    company: "All India Council for Technical Education",
    location: "India",
    isRemote: false,
    stipendLabel: "Stipend varies by employer",
    duration: "Varies",
    deadline: null,
    applyUrl: "https://internship.aicte-india.org/",
    description:
      "The All India Council for Technical Education's official internship portal, listing internships for students across India. Students can search by city, sector, and duration.",
    postedAt: null,
    sourceName: "AICTE",
    tags: ["India", "Government", "Internships"],
  },
  {
    id: "program-nats",
    kind: "INTERNSHIP",
    title: "National Apprenticeship Training Scheme (NATS)",
    company: "Government of India",
    location: "India",
    isRemote: false,
    stipendLabel: "Government-supported stipend",
    duration: "1 year",
    deadline: null,
    applyUrl: "https://nats.education.gov.in/",
    description:
      "A Government of India apprenticeship program for fresh graduates and diploma holders, with one-year training positions in public and private organizations across the country.",
    postedAt: null,
    sourceName: "NATS",
    tags: ["India", "Apprenticeship", "Government"],
  },
  {
    id: "program-pm-internship",
    kind: "INTERNSHIP",
    title: "PM Internship Scheme",
    company: "Ministry of Corporate Affairs",
    location: "India",
    isRemote: false,
    stipendLabel: "Monthly stipend + one-time grant",
    duration: "12 months",
    deadline: null,
    applyUrl: "https://pminternship.mca.gov.in/",
    description:
      "The Prime Minister's Internship Scheme offers 12-month internships in top Indian companies for youth aged 21–24, with a monthly stipend and a one-time grant. Applications are made through the official portal.",
    postedAt: null,
    sourceName: "PM Internship Scheme",
    tags: ["India", "Government", "Internships", "Paid"],
  },
  {
    id: "program-nvidia-fellowship",
    kind: "FELLOWSHIP",
    title: "NVIDIA Graduate Fellowship",
    company: "NVIDIA",
    location: "Global",
    isRemote: false,
    stipendLabel: "Fellowship funding",
    duration: "1 academic year",
    deadline: null,
    applyUrl: "https://research.nvidia.com/graduate-fellowships",
    description:
      "NVIDIA's PhD fellowship supports graduate research in accelerated computing, AI, graphics, and related fields for one academic year. Fellows are paired with an NVIDIA researcher.",
    postedAt: null,
    sourceName: "NVIDIA Research",
    tags: ["Research", "Fellowship", "AI/ML", "Global"],
  },
  {
    id: "program-google-scholarships",
    kind: "SCHOLARSHIP",
    title: "Google Scholarships",
    company: "Google",
    location: "Global (varies by region)",
    isRemote: false,
    stipendLabel: "Scholarship award",
    duration: "1 academic year",
    deadline: null,
    applyUrl: "https://buildyourfuture.withgoogle.com/scholarships",
    description:
      "Google's scholarship programs for computer science students, including the Generation Google Scholarship and awards for students with disabilities. Eligibility and award amounts vary by region.",
    postedAt: null,
    sourceName: "Google Build Your Future",
    tags: ["Scholarship", "Global", "Computer Science", "Women in Tech"],
  },
  {
    id: "program-season-of-docs",
    kind: "PROGRAM",
    title: "Google Season of Docs",
    company: "Google Open Source",
    location: "Remote / Global",
    isRemote: true,
    stipendLabel: "Paid stipend",
    duration: "3–6 months",
    deadline: null,
    applyUrl: "https://developers.google.com/season-of-docs",
    description:
      "Google's annual program that pairs technical writers with open-source organizations for mentored documentation projects. Writers receive a stipend set by the participating organization.",
    postedAt: null,
    sourceName: "Google Season of Docs",
    tags: ["Open Source", "Technical Writing", "Mentorship", "Global"],
  },
  {
    id: "program-mlsa",
    kind: "PROGRAM",
    title: "Microsoft Learn Student Ambassadors",
    company: "Microsoft",
    location: "India / Global",
    isRemote: true,
    stipendLabel: "No stipend; program benefits",
    duration: "Rolling",
    deadline: null,
    applyUrl: "https://studentambassadors.microsoft.com/",
    description:
      "A global community program for student leaders who learn and teach Microsoft technologies on campus. Ambassadors get access to learning resources, events, and a global student community.",
    postedAt: null,
    sourceName: "Microsoft Learn Student Ambassadors",
    tags: ["Community", "Microsoft", "Leadership", "Global"],
  },
];

export function getCuratedProgrammes() {
  return {
    source: "Official programmes",
    ok: true,
    status: 200,
    items: CURATED_PROGRAMMES.map((programme) => ({
      ...programme,
      tags: [...new Set([programme.kind, ...programme.tags])],
    })),
  };
}

const UNSTOP_SEARCH_URL = "https://unstop.com/api/public/opportunity/search-result";
const UNSTOP_PAGE_SIZE = 10;
const UNSTOP_PAGES = 12;
const UNSTOP_PAGE_DELAY_MS = 300;

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

/**
 * Unstop flags each required skill with whether their own AI invented it. A skill
 * the organiser never typed is not something the posting asked for, so only the
 * curated rows are read: a match score built on invented skills would be a
 * number no student can verify against the listing.
 */
function unstopCuratedSkills(row) {
  return (row.required_skills || [])
    .filter((skill) => skill?.pivot?.ai_generated !== true)
    .map((skill) => (skill.skill_name || skill.skill || "").trim())
    .filter(Boolean);
}

const TECHNICAL_WORK_FUNCTION =
  /\b(software|front[- ]?end|back[- ]?end|full[- ]?stack|mobile|web develop|information technology|engineering & technology|technology & coding|data science|data analyt|business analyt|machine learning|deep learning|artificial intelligence|applied ai|cloud|devops|qa|testing|cyber|security|ui\/ux|design and innovation|android|ios|kotlin|python|java)\b/i;

const TECHNICAL_ROLE_TITLE =
  /\b(software|programmer|programming|developer|web|front[- ]?end|back[- ]?end|full[- ]?stack|mobile|android|ios|flutter|react|node|python|java|kotlin|sql|machine learning|artificial intelligence|\bai\b|\bml\b|data science|data analy|business analy|devops|cloud|qa|quality assurance|testing|cyber|\bui\b|\bux\b|figma|blockchain|\biot\b|embedded|\bigpt\b)\b/i;

export function unstopLocation(row) {
  const places = (row.locations || [])
    .map((place) => [place.city, place.state, place.country].filter(Boolean).join(", "))
    .filter(Boolean);
  const workFromHome = row.jobDetail?.type === "wfh";
  const openAnywhere = /online/i.test(row.region || "") || workFromHome;
  if (places.length) {
    // Only jobDetail says work-from-home; region "online" means candidates apply
    // online, which is not a claim that the internship itself is remote.
    return `${places[0]}${places.length > 1 ? ` (+${places.length - 1} more)` : ""}${workFromHome ? " / WFH" : ""}`;
  }
  return openAnywhere ? "Remote" : "";
}

/**
 * Relevance is judged on the role, not the company blurb: a sales internship at
 * an AI startup names AI in its "About us" paragraph, and that is not what the
 * role asks of a student. A technical work function or a technical title
 * qualifies; prose about the product does not.
 */
export function isTechnicalUnstopRole({ title, workFunctions = [] }) {
  return TECHNICAL_WORK_FUNCTION.test(workFunctions.join(" | ")) || TECHNICAL_ROLE_TITLE.test(title || "");
}

export function unstopStipendLabel(jobDetail) {
  if (!jobDetail || jobDetail.not_disclosed) return "Stipend not listed";
  const rupee = !jobDetail.currency || jobDetail.currency === "fa-rupee" ? "₹" : "";
  const minimum = Number(jobDetail.min_salary);
  const maximum = Number(jobDetail.max_salary);
  if (Number.isFinite(minimum) && minimum > 0) {
    const per = jobDetail.pay_in === "monthly" ? "month" : jobDetail.pay_in || "stint";
    const amount =
      Number.isFinite(maximum) && maximum > minimum
        ? `${rupee}${minimum.toLocaleString("en-IN")}-${rupee}${maximum.toLocaleString("en-IN")}`
        : `${rupee}${minimum.toLocaleString("en-IN")}`;
    return `${amount}/month`;
  }
  if (jobDetail.paid_unpaid === "paid") return "Paid internship";
  if (jobDetail.paid_unpaid === "unpaid") return "Unpaid internship";
  return "Stipend not listed";
}

/**
 * Unstop is where Indian students actually find internships, and its public
 * search endpoint carries what no ATS board does: an apply-by date, a real
 * stipend, the organiser's own required skills, and who is eligible.
 *
 * The feed is ordered by last touch, and organisers keep old listings marked
 * open, so depth past a page with no live deadline only buys requests.
 */
export async function getUnstopInternships({ pages = UNSTOP_PAGES } = {}) {
  const now = Date.now();
  const isLive = (row) => !row.end_date || Date.parse(row.end_date) >= now;
  const rows = [];
  let status = 0;
  for (let page = 1; page <= pages; page++) {
    const result = await fetchJson(
      `${UNSTOP_SEARCH_URL}?opportunity=internships&page=${page}&per-page=${UNSTOP_PAGE_SIZE}`,
    );
    status = result.status;
    const batch = result.data?.data?.data;
    if (!Array.isArray(batch)) {
      if (!rows.length) return { source: "Unstop", ok: false, status, items: [] };
      break;
    }
    rows.push(...batch);
    const liveOnPage = batch.filter(isLive).length;
    if (batch.length < UNSTOP_PAGE_SIZE || liveOnPage === 0) break;
    await sleep(UNSTOP_PAGE_DELAY_MS);
  }

  const items = [];
  for (const row of rows) {
    if (row.visibility !== "public" || Number(row.regn_open) !== 1) continue;
    if (!row.title || !row.seo_url) continue;
    const curated = unstopCuratedSkills(row);
    const workFunctions = (row.workfunction || []).map((item) => (item.name || "").trim()).filter(Boolean);
    const eligibleFor = (row.filters || [])
      .filter((item) => item.type === "eligible")
      .map((item) => item.name)
      .filter(Boolean);
    const body = stripHtml(row.details);
    const location = unstopLocation(row);
    if (!location || !isEligibleLocation(location)) continue;
    if (!isTechnicalUnstopRole({ title: row.title, workFunctions })) continue;
    const company = (row.organisation?.name || "").trim();
    items.push({
      id: `un-${row.id}`,
      kind: "INTERNSHIP",
      title: row.title.trim(),
      company: company || "Unstop organiser",
      location,
      isRemote: /remote|online|wfh/i.test(location) || row.jobDetail?.type === "wfh",
      stipendLabel: unstopStipendLabel(row.jobDetail),
      duration: "See official page",
      deadline: row.end_date || null,
      applyUrl: row.seo_url,
      description:
        truncate(body, 900) ||
        `${company || "An organiser on Unstop"} is hiring for ${row.title}. See the official listing for eligibility and stipend.`,
      skillText: `${row.title} ${curated.join(" ")} ${body}`,
      requiredSkillsText: curated.join(" | "),
      postedAt: row.updated_at || null,
      sourceName: "Unstop",
      tags: [
        ...new Set([
          ...workFunctions,
          ...eligibleFor,
          ...deriveTags(`${row.title} ${body}`),
          ...locationTag(location),
        ]),
      ],
    });
  }
  return { source: "Unstop", ok: true, status, items };
}
