# Dataset Sources & Mapping Notes

The production model ships with a **synthetic** cohort because no opt-in,
anonymized placement outcomes exist yet. This file lists public sources that
are useful for calibration research, feature selection and sanity checks — not
for direct training, because their schema does not match the app's 14 on-device
features.

## 1. Public datasets worth studying

| Source | What it contains | How it helps | Caveat |
|---|---|---|---|
| [Kaggle — Factors Affecting Campus Placement](https://www.kaggle.com/datasets/benroshan/factors-affecting-campus-placement) | ~2,150 students, degree/branch, CGPA, aptitude scores, placement status | Validate which demographic/academic signals carry weight; sanity-check base rates | Small, single-campus, no app-behaviour features |
| [UCI — Student Performance (ID 320)](https://archive.ics.uci.edu/dataset/320/student+performance) | 1,044 secondary-school students, grades, study time, absences, family context | Study-time → outcome relationships; feature-engineering ideas | Secondary school, not higher-ed placement |
| [data.gov.in — Education](https://www.data.gov.in/catalog/education) | Indian higher-education enrolment/placement statistics (varies by catalogue entry) | Tier/college base-rate priors | Aggregated; verify licence before use |

**Do not** concatenate these into `placement_cohort_v1.csv`: their columns are
not the app's features, and mixing populations silently corrupts calibration.

## 2. First-party data collection (the real unlock)

The model becomes genuinely predictive only with outcomes from **your own
users**, with explicit consent:

- `applications.status` transitions to `OFFERED` / `ACCEPTED` / `REJECTED`
- `interview_reports` volume and round outcomes
- `score_snapshots` trajectory over the season
- DSA solved/attempted counts at the time of the outcome
- college tier and semester (never name, email, or ID)

Collect the 14 features **at the moment the outcome is recorded**, not at
export time — otherwise you leak future behaviour into the training label.

### Minimum viable cohort

| Requirement | Target |
|---|---|
| Labelled students (placed / not placed) | ≥ 1,500 |
| Positive rate | 20–80% |
| Colleges represented | ≥ 5 tiers |
| Missing-value rate per feature | < 10% |
| Consent coverage | 100% (opt-in, revocable) |

## 3. Fairness checks before shipping a retrained model

Compute ROC-AUC and calibration per college tier and gender (if collected).
Ship only if:

- overall ROC-AUC ≥ 0.70
- expected calibration error ≤ 0.05
- no subgroup ROC-AUC gap > 0.10
- no subgroup calibration gap > 0.08

Regressions here are product risks, not just metrics: a systematically
under-confident model discourages students who need the most help.

## 4. Labels and leakage rules

- **Label:** `placed = 1` if the student accepted or received an offer in the
  season, else 0. Do not use "applied" as a proxy.
- **Leakage:** exclude any feature computed after the outcome date.
- **Duplicates:** one row per student-season; drop the latest partial season.
- **Retraining cadence:** quarterly, with the previous model kept in git for
  rollback and drift comparison.

## 5. Regulatory notes (India DPDP + GDPR)

- Placement outcome + caste/gender/income are sensitive; collect only what the
  model needs, and document the purpose.
- Users must be able to delete their training contribution; store a salted
  consent token, not raw identity, in the research export.
- Publish a model card (see `README.md`) and a plain-language explanation of
  what the readiness score does and does not mean.
