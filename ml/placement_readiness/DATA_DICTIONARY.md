# Placement Readiness Dataset — Data Dictionary

> Dataset: `datasets/placement_cohort_v1.csv` · Manifest:
> `datasets/placement_cohort_v1.manifest.json` · Generator:
> `generate_dataset.py` · Trainer: `train.py`

## Provenance & status

This is a **synthetic, reproducible** cohort generated with a fixed seed. It is
explicitly *not* real student data. It exists to give the model a stable,
auditable baseline and to make CI reproducible. Replacing it with consented,
anonymized real placement data is a tracked follow-up (see ADR-010).

## Schema

| # | Column | Type | Range / values | Meaning |
|---|---|---|---|---|
| 1 | `cgpa` | float | 4.0–10.0 | Undergraduate CGPA |
| 2 | `dsa_solved` | int | 0–800 | Distinct DSA problems solved |
| 3 | `projects` | int | 0–12 | Substantial projects shipped |
| 4 | `internships` | int | 0–4 | Internships completed |
| 5 | `certifications` | int | 0–10 | Relevant certifications completed |
| 6 | `mock_interviews` | int | 0–30 | Mock interviews practised |
| 7 | `communication_score` | float | 0–10 | Self/peer-rated communication |
| 8 | `aptitude_score` | float | 0–100 | Aptitude assessment score |
| 9 | `backlogs` | int | 0–6 | Active backlogs |
| 10 | `tier` | int | 1–3 | College tier (1 = highest) |
| 11 | `branch_cs` | 0/1 | 0, 1 | CS/IT branch indicator |
| 12 | `github_activity` | float | 0–10 | Normalized GitHub activity |
| 13 | `applications` | int | 0–120 | Applications submitted |
| 14 | `interview_conversion` | float | 0–1 | Historical interview→offer rate |
| 15 | `placed` | 0/1 | 0, 1 | **Target** — placement outcome |

## Generation contract

- Fixed seed (`42`) and fixed row count (**24,000**).
- Positive rate ≈ **0.501** (balanced by construction).
- `manifest.json` records `rows`, `positive_rate`, and `sha256`; CI asserts all
  three and fails if `datasets/` changes after regeneration.
- `generate_dataset.py` must remain deterministic across NumPy versions (no
  unseeded randomness, no time-dependent values).

## Training & export

- Model: standardized logistic regression (`C=0.5`, `max_iter=2000`).
- Holdout: 20% stratified split, `random_state=42`.
- CI floor: **ROC-AUC > 0.80** on the holdout.
- `train.py` exports coefficients to `artifacts/model.json` and the Kotlin
  `PlacementReadinessModel`; golden vectors assert Python↔Kotlin parity.

## Intended use & limitations

- **Use:** relative readiness scoring, explainable drivers, what-if simulation.
- **Do not use:** admissions, hiring rejections, or any high-stakes decision
  without human review.
- **Limitations:** synthetic distributions may not reflect the real population;
  self-reported features (communication, GitHub activity) are noisy; the model
  is a baseline, not a causal claim.
- **Fairness:** `tier`, `branch_cs`, and `backlogs` are sensitive proxies.
  Monitor subgroup calibration before any production decisioning.
