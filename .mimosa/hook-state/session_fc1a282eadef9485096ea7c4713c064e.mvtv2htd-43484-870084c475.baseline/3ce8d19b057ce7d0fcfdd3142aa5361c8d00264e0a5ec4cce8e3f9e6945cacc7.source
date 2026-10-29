#!/usr/bin/env bash
# ============================================================
# verify_cert_pins.sh — Verify that certificate pins match
# ============================================================
# Usage:  ./scripts/verify_cert_pins.sh <hostname> <sha256-pin> [sha256-pin...]
# Example: ./scripts/verify_cert_pins.sh api.nexusapp.in ABC123... DEF456...
#
# Exits 0 if the server's current cert matches at least one pin.
# ============================================================
set -euo pipefail

HOST="${1:?Usage: $0 <hostname> <pin> [pin...]}"
shift
PINS=("$@")

if [ ${#PINS[@]} -eq 0 ]; then
  echo "ERROR: At least one SHA-256 pin is required."
  exit 1
fi

echo "=== Verifying certificate pins for $HOST ==="

ACTUAL=$(openssl s_client -connect "${HOST}:443" -servername "${HOST}" </dev/null 2>/dev/null \
  | openssl x509 -pubkey -noout \
  | openssl pkey -pubin -outform der \
  | openssl dgst -sha256 -binary \
  | base64)

echo "Actual pin:    sha256/${ACTUAL}"

MATCH=false
for PIN in "${PINS[@]}"; do
  echo "Checking pin:  sha256/${PIN}"
  if [ "${ACTUAL}" = "${PIN}" ]; then
    MATCH=true
    echo "  -> MATCH!"
  fi
done

if [ "$MATCH" = true ]; then
  echo ""
  echo "SUCCESS: At least one pin matches the server certificate."
  exit 0
else
  echo ""
  echo "FAILURE: No pins match the server certificate. Update the pins in NetworkModule.kt."
  exit 1
fi
