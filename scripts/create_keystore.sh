#!/usr/bin/env bash
# ============================================================
# create_keystore.sh — Generate a release keystore
# ============================================================
# Usage:  ./scripts/create_keystore.sh [output-path]
# Default output: ./nexus-release.keystore
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
OUTPUT="${1:-${PROJECT_DIR}/nexus-release.keystore}"

if [ -f "$OUTPUT" ]; then
  echo "Keystore already exists at: $OUTPUT"
  read -r -p "Overwrite? [y/N] " CONFIRM
  if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
    echo "Aborted."
    exit 0
  fi
fi

echo "=== Creating Release Keystore ==="
echo "Output: $OUTPUT"
echo ""

read -r -p "Key alias [nexus-key]: " KEY_ALIAS
KEY_ALIAS="${KEY_ALIAS:-nexus-key}"

read -r -p "Validity (years) [25]: " VALIDITY
VALIDITY="${VALIDITY:-25}"

read -r -p "Distinguished Name [CN=NEXUS, OU=Engineering, O=NEXUS, L=Bangalore, ST=KA, C=IN]: " DNAME
DNAME="${DNAME:-CN=NEXUS, OU=Engineering, O=NEXUS, L=Bangalore, ST=KA, C=IN}"

keytool -genkey -v \
  -keystore "$OUTPUT" \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 4096 \
  -validity "$((VALIDITY * 365))" \
  -dname "$DNAME"

echo ""
echo "=== Keystore Created ==="
echo "Path:  $OUTPUT"
echo "Alias: $KEY_ALIAS"
echo ""
echo "Set these environment variables or add to local.properties:"
echo "  KEYSTORE_PATH=$OUTPUT"
echo "  KEY_ALIAS=$KEY_ALIAS"
echo "  STORE_PASSWORD=<password you entered>"
echo "  KEY_PASSWORD=<password you entered>"
