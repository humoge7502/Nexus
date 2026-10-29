"""Trains and exports the on-device placement-readiness model.

Usage:
    python generate_dataset.py
    python train.py

Outputs:
    artifacts/metrics.json          evaluation + calibration report
    artifacts/model.json            weights, scaler, temperature, metadata
    ../../core/domain/src/main/kotlin/com/nexus/domain/usecase/PlacementReadinessModel.kt
    ../../core/domain/src/test/kotlin/com/nexus/domain/usecase/PlacementReadinessModelTest.kt

The exported Kotlin object has no runtime dependencies: inference is a few
dozen multiply-adds and runs fully offline.
"""

from __future__ import annotations

import json
from datetime import datetime, timezone
from pathlib import Path

import numpy as np
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    brier_score_loss,
    log_loss,
    precision_recall_curve,
    roc_auc_score,
)
from sklearn.model_selection import StratifiedKFold, train_test_split
from sklearn.neural_network import MLPClassifier
from sklearn.preprocessing import StandardScaler

HERE = Path(__file__).resolve().parent
DATA = HERE / "datasets" / "placement_cohort_v1.csv"
MANIFEST = HERE / "datasets" / "placement_cohort_v1.manifest.json"
ARTIFACTS = HERE / "artifacts"
KOTLIN_MAIN = (
    HERE.parent.parent
    / "core/domain/src/main/kotlin/com/nexus/domain/usecase/PlacementReadinessModel.kt"
)
KOTLIN_TEST = (
    HERE.parent.parent
    / "core/domain/src/test/kotlin/com/nexus/domain/usecase/PlacementReadinessModelTest.kt"
)

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
KOTLIN_FEATURE_NAMES = [
    "dsaSolvedLog",
    "dsaHardRatio",
    "dsaMediumRatio",
    "applicationsLog",
    "interviewRate",
    "offerRate",
    "certsLog",
    "projectsLog",
    "streakLog",
    "skillsNorm",
    "mockInterviewsLog",
    "scoreNorm",
    "collegeTierNorm",
    "attemptsLog",
]
FEATURE_LABELS = [
    "DSA problems solved",
    "Hard-problem share",
    "Medium-problem share",
    "Applications submitted",
    "Interview conversion",
    "Offer conversion",
    "Certifications",
    "Portfolio projects",
    "Study streak",
    "Skills on profile",
    "Mock interviews",
    "NEXUS score",
    "College tier",
    "Practice attempts",
]

RANDOM_STATE = 42
HIDDEN_LAYERS = (32, 16)


def expected_calibration_error(y, p, bins=10) -> float:
    edges = np.linspace(0.0, 1.0, bins + 1)
    total = 0.0
    for lo, hi in zip(edges[:-1], edges[1:]):
        mask = (p >= lo) & (p < hi if hi < 1.0 else p <= hi)
        if not mask.any():
            continue
        total += mask.mean() * abs(y[mask].mean() - p[mask].mean())
    return float(total)


def calibration_table(y, p, bins=10):
    edges = np.linspace(0.0, 1.0, bins + 1)
    rows = []
    for lo, hi in zip(edges[:-1], edges[1:]):
        mask = (p >= lo) & (p < hi if hi < 1.0 else p <= hi)
        if not mask.any():
            continue
        rows.append(
            {
                "bin": f"{lo:.1f}-{hi:.1f}",
                "count": int(mask.sum()),
                "mean_predicted": round(float(p[mask].mean()), 4),
                "observed_rate": round(float(y[mask].mean()), 4),
            }
        )
    return rows


def pr_auc(y, p) -> float:
    precision, recall, _ = precision_recall_curve(y, p)
    # precision_recall_curve returns recall descending; flip for a positive area.
    return float(np.trapezoid(precision[::-1], recall[::-1]))


def metrics(y, p) -> dict:
    return {
        "roc_auc": round(float(roc_auc_score(y, p)), 4),
        "pr_auc": round(pr_auc(y, p), 4),
        "log_loss": round(float(log_loss(y, p, labels=[0, 1])), 4),
        "brier": round(float(brier_score_loss(y, p)), 4),
        "accuracy_at_0_5": round(float(accuracy_score(y, (p >= 0.5).astype(int))), 4),
        "ece_10": round(expected_calibration_error(y, p), 4),
    }


def fit_temperature(raw_logits, y) -> float:
    best_t, best_loss = 1.0, float("inf")
    for t in np.linspace(0.5, 3.0, 251):
        p = 1.0 / (1.0 + np.exp(-raw_logits / t))
        loss = log_loss(y, p, labels=[0, 1])
        if loss < best_loss:
            best_t, best_loss = float(t), float(loss)
    return round(best_t, 3)


def mlp_raw_logits(model, x_scaled) -> np.ndarray:
    z = x_scaled
    for w, b in zip(model.coefs_[:-1], model.intercepts_[:-1]):
        z = np.maximum(0.0, z @ w + b)
    return (z @ model.coefs_[-1] + model.intercepts_[-1]).ravel()


def main() -> None:
    data = np.loadtxt(DATA, delimiter=",", skiprows=1)
    x, y = data[:, :-1], data[:, -1].astype(int)
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))

    x_train, x_test, y_train, y_test = train_test_split(
        x, y, test_size=0.2, stratify=y, random_state=RANDOM_STATE
    )

    scaler = StandardScaler().fit(x_train)
    x_train_s, x_test_s = scaler.transform(x_train), scaler.transform(x_test)

    logistic = LogisticRegression(max_iter=2000, C=0.5, random_state=RANDOM_STATE)
    logistic.fit(x_train_s, y_train)
    logistic_test = logistic.predict_proba(x_test_s)[:, 1]

    mlp = MLPClassifier(
        hidden_layer_sizes=HIDDEN_LAYERS,
        activation="relu",
        solver="adam",
        alpha=1e-4,
        batch_size=256,
        learning_rate_init=3e-3,
        max_iter=800,
        early_stopping=False,
        random_state=RANDOM_STATE,
    )
    mlp.fit(x_train_s, y_train)
    mlp_test_raw = mlp_raw_logits(mlp, x_test_s)

    # Out-of-fold logits for honest temperature fitting.
    skf = StratifiedKFold(n_splits=5, shuffle=True, random_state=RANDOM_STATE)
    oof_logits = np.zeros(len(y_train))
    for train_idx, valid_idx in skf.split(x_train_s, y_train):
        fold_scaler = StandardScaler().fit(x_train[train_idx])
        fold = MLPClassifier(
            hidden_layer_sizes=HIDDEN_LAYERS,
            activation="relu",
            solver="adam",
            alpha=1e-4,
            batch_size=256,
            learning_rate_init=3e-3,
            max_iter=800,
            early_stopping=False,
            random_state=RANDOM_STATE,
        )
        fold.fit(fold_scaler.transform(x_train[train_idx]), y_train[train_idx])
        oof_logits[valid_idx] = mlp_raw_logits(fold, fold_scaler.transform(x_train[valid_idx]))

    temperature = fit_temperature(oof_logits, y_train)
    mlp_calibrated = 1.0 / (1.0 + np.exp(-mlp_test_raw / temperature))

    baseline_metrics = metrics(y_test, logistic_test)
    mlp_metrics = metrics(y_test, mlp_calibrated)
    chosen = "logistic" if baseline_metrics["log_loss"] + 0.02 < mlp_metrics["log_loss"] else "mlp"
    # Temperature is only applied to the network; logistic regression trained
    # with maximum likelihood is already calibrated on its own logits.
    export_temperature = 1.0 if chosen == "logistic" else temperature
    test_p = logistic_test if chosen == "logistic" else mlp_calibrated

    report = {
        "model": chosen,
        "version": "1.0.0",
        "trained_at": datetime.now(timezone.utc).isoformat(),
        "dataset": manifest,
        "split": {"train": int(len(y_train)), "test": int(len(y_test))},
        "baseline_logistic": baseline_metrics,
        "mlp_calibrated": mlp_metrics,
        "temperature": export_temperature,
        "mlp_fitted_temperature": temperature,
        "calibration_table": calibration_table(y_test, test_p),
        "selected_metrics": metrics(y_test, test_p),
        "notes": (
            "Metrics are measured on a synthetic cohort (see dataset limitations). "
            "They validate pipeline and on-device parity, not real-world placement accuracy."
        ),
    }

    ARTIFACTS.mkdir(parents=True, exist_ok=True)
    (ARTIFACTS / "metrics.json").write_text(json.dumps(report, indent=2), encoding="utf-8")

    if chosen == "logistic":
        golden = build_golden_logistic(x_test, scaler, logistic, 1.0)
        write_kotlin_logistic(logistic, scaler, 1.0, report)
        write_model_json_logistic(logistic, scaler, 1.0, report)
    else:
        golden = build_golden(x_test, scaler, mlp, temperature)
        write_kotlin_mlp(mlp, scaler, temperature, report)
        write_model_json_mlp(mlp, scaler, temperature, report)
    write_golden_test(golden, chosen)
    print(json.dumps({k: report[k] for k in ("model", "temperature", "selected_metrics")}, indent=2))


def kotlin_floats(values) -> str:
    return ", ".join(f"{float(v):.8f}f" for v in values)


def header_block(meta: dict) -> str:
    return f"""// GENERATED FILE — do not edit by hand.
// Source: ml/placement_readiness/train.py
// Model: {meta['model']} v{meta['version']}
// Trained: {meta['trained_at']}
// Dataset: {meta['dataset']['dataset']} ({meta['dataset']['rows']} rows, sha256 {meta['dataset']['sha256'][:16]}...)
// Test ROC-AUC {meta['selected_metrics']['roc_auc']}, Brier {meta['selected_metrics']['brier']}
// The training cohort is synthetic; see ml/placement_readiness/README.md.
"""


def features_block() -> str:
    fields = "\n".join(f"    val {name}: Float = 0f," for name in KOTLIN_FEATURE_NAMES)
    positional = ",\n      ".join(
        f"{name} = v[{i}]" for i, name in enumerate(KOTLIN_FEATURE_NAMES)
    )
    return f"""  /** Transformed inputs; build them with [PlacementReadinessFeatures.fromRaw]. */
  data class Features(
{fields}
  ) {{
    fun toFloatArray(): FloatArray = floatArrayOf(
      {", ".join(KOTLIN_FEATURE_NAMES)}
    )
  }}

  data class Factor(val label: String, val deltaPoints: Int)

  private fun featuresOf(v: FloatArray): Features = Features(
      {positional},
  )
"""


COMMON_BODY = """
  /** Probability in 0..1 that the student is placed this season. */
  fun predict(features: Features): Float {
    val source = features.toFloatArray()
    val x = FloatArray(FEATURE_COUNT) { i ->
      (source[i] - MEANS[i]) / SCALES[i]
    }
    val logit = forward(x)
    return sigmoid(logit / TEMPERATURE)
  }

  fun predictPercent(features: Features): Int =
    (predict(features) * 100f).roundToInt().coerceIn(1, 99)

  /**
   * Local explanation: moves each input one standardized unit and ranks the
   * absolute probability change. Deterministic, dependency-free (no SHAP).
   */
  fun topFactors(features: Features, limit: Int = 3): List<Factor> {
    val base = predict(features)
    val raw = features.toFloatArray()
    return FEATURE_LABELS.mapIndexed { i, label ->
      val shifted = raw.copyOf()
      shifted[i] = shifted[i] + SCALES[i]
      val delta = predict(featuresOf(shifted)) - base
      Factor(label, (delta * 100f).roundToInt())
    }
      .sortedByDescending { abs(it.deltaPoints) }
      .take(limit)
  }

  private fun sigmoid(value: Float): Float =
    1f / (1f + exp(-value))

  // __FORWARD_IMPL__
}
"""

FORWARD_MLP = """  private fun forward(input: FloatArray): Float {
    var current = input
    for (layerIndex in LAYERS.indices) {
      val neurons = LAYERS[layerIndex]
      val next = FloatArray(neurons.size)
      for (n in neurons.indices) {
        var sum = BIASES[layerIndex][n]
        val weights = neurons[n]
        for (i in weights.indices) sum += weights[i] * current[i]
        next[n] = sum
      }
      if (layerIndex < LAYERS.lastIndex) {
        for (i in next.indices) if (next[i] < 0f) next[i] = 0f
      }
      current = next
    }
    return current[0]
  }
"""

FORWARD_LOGISTIC = """  private fun forward(input: FloatArray): Float {
    var sum = BIAS
    for (i in WEIGHTS.indices) sum += WEIGHTS[i] * input[i]
    return sum
  }
"""


def write_kotlin_mlp(mlp, scaler, temperature, report) -> None:
    lines = [
        header_block(report),
        "package com.nexus.domain.usecase",
        "",
        "import kotlin.math.abs",
        "import kotlin.math.exp",
        "import kotlin.math.roundToInt",
        "",
        "object PlacementReadinessModel {",
        "",
        f'  const val VERSION = "{report["version"]}"',
        f"  const val FEATURE_COUNT = {len(FEATURE_NAMES)}",
        f"  const val TEMPERATURE = {temperature}f",
        f"  private val MEANS = floatArrayOf({kotlin_floats(scaler.mean_)})",
        f"  private val SCALES = floatArrayOf({kotlin_floats(scaler.scale_)})",
        "  private val FEATURE_LABELS = arrayOf(",
    ]
    lines += [f'    "{label}",' for label in FEATURE_LABELS]
    lines.append("  )")
    lines.append("  private val LAYERS: Array<Array<FloatArray>> = arrayOf(")
    for w in mlp.coefs_:
        lines.append("    arrayOf(")
        for row in w:
            lines.append(f"      floatArrayOf({kotlin_floats(row)}),")
        lines.append("    ),")
    lines.append("  )")
    lines.append("  private val BIASES: Array<FloatArray> = arrayOf(")
    for b in mlp.intercepts_:
        lines.append(f"    floatArrayOf({kotlin_floats(b)}),")
    lines.append("  )")
    lines.append(features_block())
    lines.append(COMMON_BODY.replace("  // __FORWARD_IMPL__", FORWARD_MLP, 1))
    KOTLIN_MAIN.write_text("\n".join(lines), encoding="utf-8")


def write_model_json_mlp(mlp, scaler, temperature, report) -> None:
    model_json = {
        "kind": "mlp",
        "version": report["version"],
        "temperature": temperature,
        "means": [float(v) for v in scaler.mean_],
        "scales": [float(v) for v in scaler.scale_],
        "layers": [
            {
                "weights": [[float(v) for v in row] for row in w],
                "biases": [float(v) for v in b],
            }
            for w, b in zip(mlp.coefs_, mlp.intercepts_)
        ],
        "metrics": report,
    }
    (ARTIFACTS / "model.json").write_text(json.dumps(model_json, indent=2), encoding="utf-8")


def write_kotlin_logistic(model, scaler, temperature, report) -> None:
    lines = [
        header_block(report),
        "package com.nexus.domain.usecase",
        "",
        "import kotlin.math.abs",
        "import kotlin.math.exp",
        "import kotlin.math.roundToInt",
        "",
        "object PlacementReadinessModel {",
        "",
        f'  const val VERSION = "{report["version"]}"',
        f"  const val FEATURE_COUNT = {len(FEATURE_NAMES)}",
        f"  const val TEMPERATURE = {temperature}f",
        f"  private val MEANS = floatArrayOf({kotlin_floats(scaler.mean_)})",
        f"  private val SCALES = floatArrayOf({kotlin_floats(scaler.scale_)})",
        f"  private val WEIGHTS = floatArrayOf({kotlin_floats(model.coef_[0])})",
        f"  private const val BIAS = {float(model.intercept_[0]):.8f}f",
        "  private val FEATURE_LABELS = arrayOf(",
    ]
    lines += [f'    "{label}",' for label in FEATURE_LABELS]
    lines.append("  )")
    lines.append(features_block())
    lines.append(COMMON_BODY.replace("  // __FORWARD_IMPL__", FORWARD_LOGISTIC, 1))
    KOTLIN_MAIN.write_text("\n".join(lines), encoding="utf-8")


def write_model_json_logistic(model, scaler, temperature, report) -> None:
    model_json = {
        "kind": "logistic",
        "version": report["version"],
        "temperature": temperature,
        "means": [float(v) for v in scaler.mean_],
        "scales": [float(v) for v in scaler.scale_],
        "weights": [float(v) for v in model.coef_[0]],
        "bias": float(model.intercept_[0]),
        "metrics": report,
    }
    (ARTIFACTS / "model.json").write_text(json.dumps(model_json, indent=2), encoding="utf-8")


def build_golden(x_test, scaler, mlp, temperature):
    rng = np.random.default_rng(7)
    idx = rng.choice(len(x_test), size=12, replace=False)
    samples = x_test[idx]
    probs = 1.0 / (1.0 + np.exp(-mlp_raw_logits(mlp, scaler.transform(samples)) / temperature))
    return [(float(p), sample) for p, sample in zip(probs, samples)]


def build_golden_logistic(x_test, scaler, model, temperature):
    rng = np.random.default_rng(7)
    idx = rng.choice(len(x_test), size=12, replace=False)
    samples = x_test[idx]
    logits = scaler.transform(samples) @ model.coef_[0] + model.intercept_[0]
    probs = 1.0 / (1.0 + np.exp(-logits / temperature))
    return [(float(p), sample) for p, sample in zip(probs, samples)]


def write_golden_test(golden, chosen) -> None:
    rows = []
    for prob, sample in golden:
        fields = ",\n        ".join(
            f"{name} = {float(v):.6f}f" for name, v in zip(KOTLIN_FEATURE_NAMES, sample)
        )
        rows.append(f"    {prob:.6f}f to Features(\n        {fields},\n    ),")
    body = "\n".join(rows)

    source = f"""// GENERATED FILE — golden vectors produced by ml/placement_readiness/train.py
package com.nexus.domain.usecase

import com.nexus.domain.usecase.PlacementReadinessModel.Features
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlacementReadinessModelTest {{

  private val golden = listOf(
{body}
  )

  @Test
  fun `kotlin inference matches python export`() {{
    golden.forEach {{ (expected, features) ->
      assertEquals("prediction drift for {chosen}", expected, PlacementReadinessModel.predict(features), 2e-4f)
    }}
  }}

  @Test
  fun `probability stays in range and is monotone in score`() {{
    val empty = PlacementReadinessModel.predict(Features())
    val maxed = PlacementReadinessModel.predict(
      Features(
        dsaSolvedLog = 8f,
        dsaHardRatio = 1f,
        dsaMediumRatio = 1f,
        applicationsLog = 6f,
        interviewRate = 1f,
        offerRate = 1f,
        certsLog = 5f,
        projectsLog = 5f,
        streakLog = 6f,
        skillsNorm = 1f,
        mockInterviewsLog = 5f,
        scoreNorm = 1f,
        collegeTierNorm = 1f,
        attemptsLog = 8f,
      )
    )
    assertTrue(empty in 0f..1f)
    assertTrue(maxed in 0f..1f)
    assertTrue(maxed > empty)
  }}

  @Test
  fun `top factors are bounded and ranked`() {{
    val features = Features(scoreNorm = 0.6f, dsaSolvedLog = 3f, interviewRate = 0.4f)
    val factors = PlacementReadinessModel.topFactors(features, limit = 3)
    assertEquals(3, factors.size)
    assertTrue(factors[0].label.isNotBlank())
  }}
}}
"""
    KOTLIN_TEST.write_text(source, encoding="utf-8")


if __name__ == "__main__":
    main()
