# Placement Readiness Model

On-device, explainable model that estimates the probability a student is
placed in the current season. Inference is pure Kotlin with no TFLite
runtime, no network, and no telemetry — the whole model is ~1 KB of
constants in `PlacementReadinessModel.kt`.

## Why not an LLM?

Placement probability must be cheap (called on every forecast refresh),
deterministic, offline-capable and explainable ("your mock-interview volume
is the biggest lever"). A 14-feature logistic model answers all four; an LLM
call answers none without cost and latency.

## Pipeline

```
generate_dataset.py  →  datasets/placement_cohort_v1.csv
                            │
train.py             →  artifacts/metrics.json, artifacts/model.json
                     →  core/domain/.../PlacementReadinessModel.kt   (generated)
                     →  core/domain/.../PlacementReadinessModelTest.kt (golden vectors)
```

Retrain:

```bash
cd ml/placement_readiness
python generate_dataset.py
python train.py
```

The app does not read `model.json` at runtime; Kotlin constants are generated
so the APK stays dependency-free and schema-stable. Golden vectors from the
held-out test split are compiled into a JVM unit test that fails if Python and
Kotlin inference drift by more than `2e-4`.

## Features (all available on-device)

| # | Feature | Source |
|---|---|---|
| 1 | `log1p(solved DSA problems)` | `DSAProblemRepository` |
| 2 | hard solved / solved | `DSAProblemRepository` |
| 3 | medium solved / solved | `DSAProblemRepository` |
| 4 | `log1p(applications submitted)` | `ApplicationRepository` |
| 5 | interviews / applications | `ApplicationRepository` |
| 6 | offers / applications | `ApplicationRepository` |
| 7 | `log1p(certifications)` | `CertificationRepository` |
| 8 | `log1p(portfolio projects)` | `PortfolioRepository` |
| 9 | `log1p(study streak days)` | `UserPreferences` |
| 10 | skills on profile / 20 | `UserPreferences` |
| 11 | `log1p(mock interviews)` | `InterviewReportRepository` |
| 12 | NEXUS score / 1000 | `ScoreSnapshotRepository` |
| 13 | college tier → `(5-tier)/4` | `UserPreferences.collegeTier` |
| 14 | `log1p(solved + attempts)` | `DSAProblemRepository` |

`PlacementReadinessFeatures` performs the mapping and is unit-tested
separately from the generated model.

## Evaluation (current export)

Dataset: 24,000 synthetic rows, seed `20261024`, positive rate 0.501.
Split: 19,200 train / 4,800 test.

| Model | ROC-AUC | PR-AUC | Log loss | Brier | Accuracy @0.5 | ECE (10 bins) |
|---|---|---|---|---|---|---|
| Logistic (selected) | **0.8614** | **0.8647** | **0.4611** | **0.1521** | **0.771** | **0.0164** |
| MLP 32→16 (calibrated) | 0.8329 | 0.8286 | 0.5068 | 0.1671 | 0.750 | 0.0218 |
| HistGradientBoosting (probe) | 0.8612 | — | 0.4622 | 0.1522 | — | — |

The logistic model wins because the data-generating process is additive in
latent traits; a boosted tree adds no signal and the MLP slightly overfits.
The pipeline still trains and compares all candidates on every run and
refuses to ship a model whose selected metrics regress.

## Honest limitations

- **The cohort is synthetic.** Metrics prove the pipeline, calibration and
  Kotlin/Python parity; they do **not** prove real-world placement accuracy.
  Do not put "87% accurate placement predictions" in marketing.
- The generator encodes an assumed causal structure (ability, diligence,
  interview skill, campus access). Real cohorts will shift coefficients.
- One model serves all tiers. Tier-level forecasts still come from
  `PlacementPredictionEngine`; the model output is shown as an independent,
  user-level readiness prior.

## Retraining on real data

1. Export opt-in, anonymized outcomes with the same 14 columns plus `placed`.
2. Run `python train.py` with `DATA` pointed at that CSV (manifest optional).
3. Review `artifacts/metrics.json`: require ROC-AUC ≥ 0.70, ECE ≤ 0.05, and
   no subgroup gap > 0.10 before shipping.
4. Bump `VERSION` in `train.py`, rerun, and keep the previous generated file
   in git history so drift is auditable.
5. Re-run `./gradlew :core:domain:testDebugUnitTest` to confirm parity.

## Privacy

Inputs stay in memory on the device. No feature vector, prediction or
explanation is logged or uploaded. The model is deterministic, so the same
profile always produces the same score — auditable by the user.
