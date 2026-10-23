#!/usr/bin/env bash
# ============================================================
# setup_secrets.sh — Configure local.properties from template
# ============================================================
# Copies local.properties.template to local.properties if it
# doesn't exist, then prompts for required secret values.
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
PROPERTIES_FILE="${PROJECT_DIR}/local.properties"
TEMPLATE_FILE="${PROJECT_DIR}/local.properties.template"

# Required properties (no defaults)
REQUIRED_KEYS=(
  "GITHUB_CLIENT_ID"
  "GITHUB_CLIENT_SECRET"
  "GOOGLE_WEB_CLIENT_ID"
)

# Optional properties (with defaults)
declare -A OPTIONAL_KEYS
OPTIONAL_KEYS[KEYSTORE_PATH]=""
OPTIONAL_KEYS[STORE_PASSWORD]=""
OPTIONAL_KEYS[KEY_ALIAS]=""
OPTIONAL_KEYS[KEY_PASSWORD]=""
OPTIONAL_KEYS[NEXUS_BACKEND_URL]="https://api.nexusapp.in/v1"
OPTIONAL_KEYS[LEETCODE_API_URL]="https://alfa-leetcode-api.onrender.com/"

if [ -f "$PROPERTIES_FILE" ]; then
  echo "Found existing local.properties. Checking for missing keys..."
else
  echo "Creating new local.properties..."
  touch "$PROPERTIES_FILE"
fi

# Load existing values
declare -A EXISTING
while IFS='=' read -r key value; do
  if [ -n "$key" ] && [ -n "$value" ]; then
    EXISTING["$key"]="$value"
  fi
done < "$PROPERTIES_FILE"

# Check required keys
MISSING=false
for KEY in "${REQUIRED_KEYS[@]}"; do
  if [ -z "${EXISTING[$KEY]:-}" ]; then
    MISSING=true
    read -r -p "Enter $KEY: " VALUE
    echo "$KEY=$VALUE" >> "$PROPERTIES_FILE"
  fi
done

# Check optional keys
for KEY in "${!OPTIONAL_KEYS[@]}"; do
  DEFAULT="${OPTIONAL_KEYS[$KEY]}"
  if [ -z "${EXISTING[$KEY]:-}" ]; then
    if [ -n "$DEFAULT" ]; then
      echo "$KEY=$DEFAULT" >> "$PROPERTIES_FILE"
      echo "Set $KEY=$DEFAULT (default)"
    else
      read -r -p "Enter $KEY (optional, press Enter to skip): " VALUE
      if [ -n "$VALUE" ]; then
        echo "$KEY=$VALUE" >> "$PROPERTIES_FILE"
      fi
    fi
  fi
done

echo ""
echo "=== local.properties is ready ==="
echo "Review the file at: $PROPERTIES_FILE"
