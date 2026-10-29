#!/bin/bash
# Seed Firestore with initial opportunity data
# Run ONCE after Firebase project is set up
# Usage: ./backend/seed/seed_firestore.sh
# Prerequisites: firebase CLI installed, authenticated, project selected
#   export GOOGLE_APPLICATION_CREDENTIALS="/path/to/serviceAccount.json"

set -e

echo "========================================"
echo "NEXUS Firestore Seed Script"
echo "========================================"
echo ""

# Check firebase CLI
if ! command -v firebase &> /dev/null; then
  echo "Firebase CLI not found."
  echo "Install: npm install -g firebase-tools"
  exit 1
fi

# Check GOOGLE_APPLICATION_CREDENTIALS
if [ -z "$GOOGLE_APPLICATION_CREDENTIALS" ]; then
  echo "GOOGLE_APPLICATION_CREDENTIALS not set."
  echo "Export path to your service account JSON:"
  echo "  export GOOGLE_APPLICATION_CREDENTIALS=\"/path/to/serviceAccount.json\""
  echo ""
  echo "Download from: Firebase Console > Project Settings > Service Accounts"
  exit 1
fi

PROJECT=$(firebase projects:list --json 2>/dev/null | python3 -c \
  "import sys,json; projects=json.load(sys.stdin); \
  print(projects[0]['projectId'] if projects else '')" 2>/dev/null || echo "")

if [ -z "$PROJECT" ]; then
  echo "Not authenticated or no project selected."
  echo "Run: firebase login && firebase use --add"
  exit 1
fi

echo "Project: $PROJECT"
echo ""

cd "$(dirname "$0")"
npm install --silent 2>/dev/null

node -e "
const admin = require('firebase-admin');
const data = require('./opportunities.json');

admin.initializeApp({
  credential: admin.credential.applicationDefault()
});

const db = admin.firestore();

async function seed() {
  const batch = db.batch();
  let count = 0;

  for (const opportunity of data.opportunities) {
    const ref = db.collection('opportunities').doc(opportunity.id);
    batch.set(ref, {
      ...opportunity,
      postedAt: admin.firestore.Timestamp.fromMillis(opportunity.postedAt),
      deadline: opportunity.deadline
        ? admin.firestore.Timestamp.fromMillis(opportunity.deadline)
        : null,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
    count++;
  }

  await batch.commit();
  console.log('Seeded ' + count + ' opportunities');
  process.exit(0);
}

seed().catch(err => {
  console.error('Seed failed:', err);
  process.exit(1);
});
"

echo ""
echo "Firestore seeded successfully!"
echo ""
echo "Verify in Firebase Console:"
echo "  https://console.firebase.google.com/project/$PROJECT/firestore"
