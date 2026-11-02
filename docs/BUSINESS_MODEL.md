# Sagitta — Business Model

**Status:** working model, 2026-11-01
**Product name:** Sagitta (the arrow constellation)
**One line:** Every verified opportunity for Indian students, in one calm feed — with the deadline, the source, and the next step.

---

## 1. The problem

Indian students do not lack opportunities. They lack a reliable way to see them in time.

- Higher-education enrolment is **4.33 crore students** (AISHE 2021-22), and the pipeline keeps growing.
- Opportunities are scattered across Internshala, Unstop, LinkedIn, company career pages, college notice boards, WhatsApp groups, Telegram channels, Instagram stories, and PDF circulars.
- The failure mode is not "no listing found". It is **finding out after the deadline**, or applying to a listing that expired three weeks ago.
- Students spend hours filtering spam, duplicate posts, unpaid "internships", and posts with no application link. The cost is attention, not just time.
- Colleges have no clean view of what their students actually apply to, and employers struggle to reach students outside a handful of tier-1 campuses.

Internshala alone reports **34 million registered users and 4.5 lakh employers** (company-reported, February 2026), and was acquired by upGrad in a 90% stock-swap deal. That proves the demand. It also shows the incumbent's centre of gravity: internships, employer postings, and course cross-sell. Unstop leads with competitions and gamified assessments. LinkedIn is general-purpose. Nobody owns the **verified, deadline-first, all-opportunity-type student feed** with a calm, trustworthy interface.

## 2. The product wedge

Sagitta is not another job board. It is an **opportunity instrument**:

1. **One feed, every opportunity type** — internships, hackathons, scholarships, research, open source, certifications, workshops.
2. **Verified before listed** — every card carries its source and a last-verified timestamp. Expired listings are removed automatically. No ghost jobs.
3. **Deadline-first** — the deadline is a first-class object, not a line in a description. Each saved or in-progress opportunity warns at a week out, at three days, the day before, the day it closes, and again in its final evening — once each, never repeated.
4. **A pipeline, not a bookmark pile** — saved → applied → interview → offer, with next actions. Students see momentum instead of a list of maybes.
5. **Calm by design** — one clear signal per screen, no infinite dopamine feed, no fake urgency. The interface is an instrument, not a casino.

The product promise: **you will not miss a real opportunity because it was buried, expired, or announced somewhere you do not follow.**

## 3. Market

| Layer | Size | Basis |
|---|---|---|
| TAM | 4.33 crore higher-education students in India | AISHE 2021-22, Ministry of Education |
| SAM | ~1.5 crore students actively seeking internships, placements, scholarships or competitions each year | Assumption: roughly one-third of enrolled students are in the active opportunity window; validate with cohort surveys |
| SOM (36 months) | 1.5–2.5 million registered students; 300–500 paying campuses; 1,000+ verified employers | Bottom-up: campus-by-campus rollout, 2–3 states per year |

India is the beachhead because the pain is acute, the supply is fragmented, and the distribution is physical (campuses) as well as digital. The model travels to other South Asian markets later; the data model and verification workflow are not India-specific.

## 4. Competitive landscape

| Player | Strength | Gap Sagitta exploits |
|---|---|---|
| Internshala / upGrad | Largest internship supply, employer relationships, training cross-sell | Internship-centric; course upsell dilutes trust; little verification transparency; deadlines are secondary |
| Unstop | Competitions, gamification, assessments | Competition-first; internships and scholarships are secondary; engagement mechanics can overwhelm signal |
| LinkedIn | Network, brand, scale | General-purpose; students drown in noise; no deadline workflow; no India-campus context |
| College placement cells | Trusted, local | Manual, spreadsheet-driven, opaque to students; no cross-campus supply |
| WhatsApp / Telegram groups | Fast, free | Unverified, duplicate-heavy, no search, no reminders, no source trail |

**Sagitta's position:** the trustworthy, deadline-first layer across all opportunity types. We do not need to out-list Internshala on day one. We need to be the place a student checks every morning because it is accurate, complete for their goals, and never wastes their time.

## 5. Business model

**Students never pay to see or apply to an opportunity. That is a hard product rule.**

Revenue comes from the supply side and from institutions that benefit when students succeed.

### 5.1 Verified employer / organiser listings (primary, year 1)

- ₹3,000 per role for 30 days; ₹24,000 for a 10-role annual pack.
- Optional featured placement: +₹2,000, always labelled **Featured** and never allowed to outrank a better match silently.
- First 500 verified employers free for 12 months to seed supply.
- Verification includes organisation identity, application URL, deadline, and stipend/prize clarity.
- Why employers pay: verified student intent, deadline-aware distribution, and a dashboard that shows applies and completions — not impressions.

### 5.2 Campus Pro (primary, year 1–2)

- ₹99 per student per year, minimum 1,000 students; ₹75,000/year flat for colleges below 1,000.
- Includes a branded campus page, cohort analytics (aggregate and consented), bulk opportunity pushes, and a placement-cell console.
- Why colleges pay: NAAC/NIRF reporting pressure, placement outcomes, and a defensible "we told every student in time" record.
- Target: 25 campuses in year 1, 150 in year 2.

### 5.3 Sagitta Pro for students (secondary, year 2)

- ₹149/month or ₹799/year.
- Advanced AI resume and cover-letter tailoring, unlimited mock interviews, portfolio reviews, application CRM exports, priority alerts.
- **Never** gates opportunities, deadlines, reminders, or applications.
- Target: 2% of active students convert.

### 5.4 Sponsored scholarships and competitions (opportunistic)

- Clearly labelled sponsored placements for mission-aligned scholarships, hackathons, and fellowships.
- No sponsored result may hide its sponsor. No pay-to-rank for employers either.

### 5.5 What we will not do

- Sell student personal data.
- Charge students to apply.
- Let employers buy their way to the top of a student's match list.
- Inflate user counts or engagement with fake urgency.
- List an opportunity without a working application URL and a traceable source.

## 6. Unit economics (illustrative, not a forecast)

Year-1 operating scenario, deliberately conservative:

| Line | Assumption | Annual |
|---|---|---|
| Employer listings | 150 listings/month average × ₹3,000 × 12 | ₹54,00,000 |
| Campus Pro | 25 campuses × 2,000 students × ₹99 | ₹49,50,000 |
| Student Pro | 100,000 MAU × 2% × ₹799 | ₹15,98,000 |
| **Revenue** | | **₹1,19,48,000** |
| Content + verification ops | 4 people × ₹6L | ₹24,00,000 |
| Engineering + product | 3 people × ₹18L | ₹54,00,000 |
| Infrastructure + tools | | ₹6,00,000 |
| Growth + campus ambassadors | | ₹20,00,000 |
| **Cost** | | **₹1,04,00,000** |

At this scale the business is near break-even, before the campus flywheel compounds. The model is deliberately not dependent on student subscriptions.

**Sensitivity:** if employer listings average 80/month, revenue falls to ~₹79L and the company is ~₹25L short. The year-1 plan therefore prioritises campus revenue and keeps the team small. The first hire after engineering should be a verification editor, not a salesperson.

## 7. Go-to-market

**Campus-led, not ad-led.**

1. **Campus ambassadors** — one paid student leader per college (₹5,000/month stipend + Pro + certificate). They run a 10-minute onboarding, share the weekly digest in existing college groups, and report broken listings.
2. **Placement-cell pilot** — free 60-day Campus Pro pilot for 20 colleges, with a clear outcome report (verified listings delivered, deadlines met, applications submitted).
3. **Creator partnerships** — study/placement creators get a real dashboard of opportunities for their audience; no fake testimonials, no undisclosed sponsorships.
4. **SEO and utility pages** — "internships closing this week", "scholarships for Indian students", "hackathons in November", each generated from the verified feed with schema markup.
5. **Referrals** — students invite classmates; both get Pro, never cash for spam.
6. **Employer supply** — start with 50 verified startups and early-career programmes; publish a public verification standard and a 48-hour review SLA.

## 8. The update loop (why students stay current)

This is a product requirement, not a marketing promise:

- **Every 6 hours:** the client syncs the signed feed when on unmetered network; on-demand refresh otherwise.
- **Every morning IST:** the feed pipeline re-validates every application URL, expires past deadlines, dedupes cross-posted roles, and publishes a new feed with a timestamp.
- **Daily 07:00 IST:** a short briefing — new matches, deadlines in the next 7 days, one suggested action.
- **Deadline reminders:** a week out, three days out, the day before, the day it closes, and once in its final evening — only for saved or in-progress opportunities, and each of those fires once. Feed deadlines are calendar days, so no reminder claims an hour the source never published.
- **Weekly:** one local retention row for the cohort metrics above. There is no second weekly notification — the daily briefing already carries every deadline inside seven days, and a second digest would compete with the reminders that matter.
- **Transparency:** every card shows source, last verified, and "report a problem". If we cannot verify it, we do not show it.

## 9. Trust, safety and privacy

- **Verification standard:** organisation identity, live application URL, explicit role, explicit location/eligibility, deadline (or "rolling" stated by the source), and stipend/prize clarity where the source provides it.
- **Source trail:** every listing stores its source name and canonical URL. Curated listings are labelled as curated.
- **Expiry:** the backend marks listings inactive after their deadline; the client hides expired entries even when offline.
- **Moderation:** community submissions go to a review queue; no auto-publish.
- **Privacy:** data minimisation, no sale of student data, consent-based campus analytics, and a deletion path. India's DPDP Act 2023 applies; the app ships a plain-language privacy policy and a Data Safety declaration.
- **Play Store:** no misleading claims, no fake urgency, no restricted permissions, and a real privacy policy and support contact before submission.

## 10. Metrics

**North star:** *deadlines met per active student per month* — a student who sees an opportunity, saves it, and applies before the deadline. It measures whether the product actually helps, not whether it manufactures engagement.

Supporting metrics:
- Verified-to-live ratio (target > 98% of shown listings have a working URL).
- Time from source publication to in-app listing (target < 24 hours).
- Reminder → application conversion.
- 4-week retention of students who save at least one opportunity.
- Campus activation: % of students who open the weekly digest.
- Employer repeat rate and time-to-fill.
- Report rate per 1,000 listings (target < 2).

## 11. Risks and mitigations

| Risk | Mitigation |
|---|---|
| Cold-start supply | Curated official programmes + public ATS feeds + 50 design-partner employers; never fabricate listings |
| Incumbent response | Win on trust and deadline reliability, not on breadth; campus distribution is slower for incumbents to copy |
| Spam / low-quality listings | Paid listings still pass verification; public reporting; expiry automation; no auto-publish |
| Seasonality (placement cycles) | Scholarships, hackathons, research and open-source programmes smooth the year |
| Regulatory (DPDP, Play policy) | Data minimisation, consent, deletion, clear privacy policy, no sensitive permissions |
| Content-ops cost | Automate URL verification and expiry; humans only review exceptions and curated programmes |

## 12. Twelve-month plan

| Quarter | Focus | Exit criterion |
|---|---|---|
| Q1 | Verified feed pipeline, rebrand, 20 design-partner employers, 5 campus pilots | 1,000 weekly active students; 98% live-link rate |
| Q2 | Campus Pro console, cohort reporting, first design-partner campuses onboarded | 25 paying campuses; 25,000 MAU |
| Q3 | Employer self-serve, sponsored programmes, creator partnerships | ₹25L quarterly revenue; 100k MAU |
| Q4 | Student Pro, outcome reporting, expansion to 2 more states | Break-even contribution margin; 150 campuses |

## 13. What this build implements now

- Rebrand and product narrative aligned to the model.
- A **real, scheduled feed** with source attribution, URL verification, expiry, and a public refresh cadence.
- In-app **application tracking** and **deadline reminders** that work from the verified feed.
- A guided tour that teaches the product in under two minutes.
- Play Store-ready listing, privacy, and data-safety material — without committing secrets.

The campus console and employer self-serve portal are deliberately **not** faked inside the student app. They are separate products and will ship as real surfaces when the first design partners are signed.

## 14. Sources

- AISHE 2021-22, Ministry of Education (PIB): https://www.pib.gov.in/PressReleasePage.aspx?PRID=1999713
- upGrad acquires Internshala (90% stock swap), Entrepreneur India, 2026-02-26: https://india.entrepreneur.com/news-and-trends/upgrad-acquires-internship-platform-internshala-in/503027
- Internshala scale (company-reported, Feb 2026): https://www.linkedin.com/posts/loveena-sirohi_a-significant-shift-just-happened-in-india-activity-7435246048421298176-4sFx
- Handshake revenue and scale: https://sacra.com/c/handshake/
- Handshake valuation: https://multiples.vc/private-comps/handshake
- Unstop internships (live count): https://unstop.com/internship
- Internshala internships (live count): https://internshala.com/internships/
- DPDP Act 2023, MeitY: https://www.meity.gov.in/data-protection-framework
- Google Play Data Safety: https://support.google.com/googleplay/android-developer/answer/10787469
