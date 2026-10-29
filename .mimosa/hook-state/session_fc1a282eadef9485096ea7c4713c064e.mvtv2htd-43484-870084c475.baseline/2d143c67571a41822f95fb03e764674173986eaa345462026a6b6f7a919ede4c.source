#!/usr/bin/env bash
# ============================================================
# extract_cert_pins.sh — Extract SHA-256 certificate pins
# ============================================================
# Usage:  ./scripts/extract_cert_pins.sh <hostname>
# Example: ./scripts/extract_cert_pins.sh api.nexusapp.in
#
# Copy the output hashes into NetworkModule.kt CertificatePinner.
# Always pin at least TWO hashes: current + backup (next cert).
# ============================================================
set -euo pipefail

HOST="${1:?Usage: $0 <hostname>}"
PORT="${2:-443}"

echo "=== Extracting certificate pins for $HOST:$PORT ==="
echo ""

openssl s_client -connect "${HOST}:${PORT}" -servername "${HOST}" </dev/null 2>/dev/null \
  | openssl x509 -pubkey -noout \
  | openssl pkey -pubin -outform der \
  | openssl dgst -sha256 -binary \
  | base64

echo ""
echo "=== Done ==="
echo "Add the output above to NetworkModule.kt CertificatePinner.Builder()"
echo "Example: .add(\"${HOST}\", \"sha256/<output>\")"
