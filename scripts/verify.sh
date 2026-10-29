#!/usr/bin/env bash
# One-command verification gate: unit tests, static analysis, production audit,
# and a debug build. CI runs the same steps as separate jobs.
set -euo pipefail

echo "==> 1/4 Unit tests"
./gradlew testDebugUnitTest --console=plain

echo "==> 2/4 Static analysis (detekt)"
./scripts/detekt.sh

echo "==> 3/4 Production-readiness audit"
./gradlew runAudit --console=plain

echo "==> 4/4 Debug build"
./gradlew :app:assembleDebug --console=plain

echo
echo "All checks passed."
echo "Audit report: build/audit/audit-report.html"
