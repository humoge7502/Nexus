"""Generates the NEXUS placement-readiness training cohort.

The data is synthetic. It is produced by a documented structural model so the
training pipeline, calibration and export can be validated end-to-end before
real (opt-in, anonymized) outcome data is available. Replace
``datasets/placement_cohort_v1.csv`` with a real cohort that has the same
columns and rerun ``train.py``; nothing else needs to change.

Columns (all features are available on-device):
  dsa_solved_log, dsa_hard_ratio, dsa_medium_ratio, applications_log,
  interview_rate, offer_rate, certs_log, projects_log, streak_log,
  skills_norm, mock_interviews_log, score_norm, college_tier_norm,
  attempts_log, placed
"""

from __future__ import annotations

import hashlib
import json
import math
from datetime import datetime, timezone
from pathlib import Path

import numpy as np

SEED = 20261024
N_ROWS = 24000
OUT_DIR = Path(__file__).resolve().parent / "datasets"


def sigmoid(x: np.ndarray) -> np.ndarray:
    return 1.0 / (1.0 + np.exp(-x))


def generate(seed: int = SEED, n: int = N_ROWS) -> tuple[np.ndarray, np.ndarray]:
    rng = np.random.default_rng(seed)

    # Latent traits (standard normal).
    ability = rng.normal(0, 1, n)
    diligence = rng.normal(0, 1, n)
    interview_skill = 0.55 * ability + 0.7 * rng.normal(0, 1, n)
    college_tier = rng.integers(1, 5, n)  # 1 = tier-1 campus, 4 = tier-4
    college_effect = {1: 0.9, 2: 0.45, 3: 0.0, 4: -0.35}
    accessibility = np.array([college_effect[int(c)] for c in college_tier])

    # Observed behaviours. Poisson rates are clipped to keep outliers sane.
    dsa_solved = rng.poisson(np.exp(np.clip(2.25 + 0.55 * ability + 0.8 * diligence, 0.2, 5.0)))
    dsa_hard_ratio = sigmoid(-1.35 + 0.75 * ability + 0.25 * diligence + rng.normal(0, 0.35, n))
    dsa_medium_ratio = np.clip(
        0.35 + 0.22 * diligence + 0.12 * ability + rng.normal(0, 0.08, n), 0.05, 0.95
    )
    applications = 1 + rng.poisson(
        np.exp(np.clip(1.45 + 0.6 * diligence + 0.35 * accessibility, 0.2, 4.2))
    )
    interview_rate = sigmoid(
        -1.55 + 0.85 * ability + 0.9 * interview_skill + 0.55 * accessibility
        + rng.normal(0, 0.3, n)
    )
    interviews = rng.binomial(np.maximum(applications, 1), interview_rate)
    offer_rate = sigmoid(-1.0 + 0.8 * interview_skill + 0.5 * ability + 0.3 * accessibility)
    offers = rng.binomial(np.maximum(interviews, 1), offer_rate)
    certs = rng.poisson(np.exp(np.clip(0.15 + 0.5 * diligence, 0.05, 2.6)))
    projects = rng.poisson(np.exp(np.clip(0.55 + 0.6 * diligence + 0.3 * ability, 0.1, 2.8)))
    streak = rng.poisson(np.exp(np.clip(2.0 + 0.75 * diligence, 0.2, 5.5)))
    streak = np.clip(streak, 0, 365)
    skills = np.clip(rng.poisson(np.exp(np.clip(1.75 + 0.45 * diligence, 0.2, 3.4))), 0, 20)
    mock_interviews = rng.poisson(np.exp(np.clip(0.9 + 0.55 * interview_skill + 0.25 * diligence, 0.05, 3.2)))
    attempts = np.maximum(dsa_solved + rng.poisson(4), 1)

    score = 500 + 110 * ability + 55 * diligence + 18 * rng.normal(0, 1, n)
    score = np.clip(score, 100, 1000)

    features = np.column_stack([
        np.log1p(dsa_solved),
        dsa_hard_ratio,
        dsa_medium_ratio,
        np.log1p(applications),
        interviews / np.maximum(applications, 1),
        offers / np.maximum(applications, 1),
        np.log1p(certs),
        np.log1p(projects),
        np.log1p(streak),
        skills / 20.0,
        np.log1p(mock_interviews),
        score / 1000.0,
        (5 - college_tier) / 4.0,
        np.log1p(attempts),
    ])

    # Placement is driven by latent traits and accessibility, not by any single
    # logged behaviour directly, so the model must combine weak signals.
    logit = (
        -0.05
        + 1.30 * ability
        + 0.70 * diligence
        + 1.05 * interview_skill
        + 0.40 * accessibility
        + 0.20 * (score - 500) / 100.0
        + rng.normal(0, 0.55, n)
    )
    placed = (sigmoid(logit) > rng.random(n)).astype(np.int64)
    return features.astype(np.float32), placed


FEATURE_NAMES = [
    "dsa_solved_log",
    "dsa_hard_ratio",
    "dsa_medium_ratio",
    "applications_log",
    "interview_rate",
    "offer_rate",
    "certs_log",
    "projects_log",
    "streak_log",
    "skills_norm",
    "mock_interviews_log",
    "score_norm",
    "college_tier_norm",
    "attempts_log",
]


def main() -> None:
    features, labels = generate()
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    out = OUT_DIR / "placement_cohort_v1.csv"

    header = ",".join(FEATURE_NAMES + ["placed"])
    with out.open("w", encoding="utf-8", newline="\n") as fh:
        fh.write(header + "\n")
        for row, label in zip(features, labels):
            fh.write(",".join(f"{float(v):.6f}" for v in row) + f",{int(label)}\n")

    digest = hashlib.sha256(out.read_bytes()).hexdigest()
    manifest = {
        "dataset": out.name,
        "rows": int(features.shape[0]),
        "features": FEATURE_NAMES,
        "positive_rate": round(float(labels.mean()), 4),
        "seed": SEED,
        "generator": "generate_dataset.py",
        "sha256": digest,
        "provenance": "synthetic_v1",
        "limitations": (
            "Synthetic cohort generated from a documented latent-trait model. "
            "Metrics measure pipeline correctness and model capacity, not real-world "
            "placement accuracy. Retrain on opt-in anonymized outcomes before "
            "making marketing claims."
        ),
    }
    (OUT_DIR / "placement_cohort_v1.manifest.json").write_text(
        json.dumps(manifest, indent=2), encoding="utf-8"
    )
    print(f"wrote {out} rows={features.shape[0]} positive_rate={labels.mean():.3f}")
    print(f"sha256={digest}")


if __name__ == "__main__":
    main()
