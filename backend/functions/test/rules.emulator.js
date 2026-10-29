"use strict";

/**
 * Firestore security-rules suite. Runs only against the Firestore emulator:
 *   npm run test:rules
 * which wraps this file in `firebase emulators:exec`.
 */

const { test, before, after, beforeEach } = require("node:test");
const path = require("node:path");
const { readFileSync } = require("node:fs");
const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const {
  doc,
  getDoc,
  setDoc,
  updateDoc,
  deleteDoc,
} = require("firebase/firestore");

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: "demo-nexus",
    firestore: {
      rules: readFileSync(
        path.resolve(__dirname, "..", "..", "firestore.rules"),
        "utf8"
      ),
    },
  });
});

after(async () => {
  await testEnv.cleanup();
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

const db = (uid, token) =>
  testEnv.authenticatedContext(uid, token).firestore();
const anon = () => testEnv.unauthenticatedContext().firestore();

async function seed(pathString, data) {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), pathString), data);
  });
}

test("unauthenticated users cannot read a private profile", async () => {
  await seed("users/alice", { displayName: "Alice" });
  await assertFails(getDoc(doc(anon(), "users/alice")));
});

test("owner reads own profile, other users cannot", async () => {
  await seed("users/alice", { displayName: "Alice" });
  await assertSucceeds(getDoc(doc(db("alice"), "users/alice")));
  await assertFails(getDoc(doc(db("mallory"), "users/alice")));
});

test("owner cannot self-grant Pro or score on create", async () => {
  const alice = db("alice");
  await assertFails(
    setDoc(doc(alice, "users/alice"), { displayName: "A", isPro: true })
  );
  await assertFails(
    setDoc(doc(alice, "users/alice"), { displayName: "A", nexusScore: 9999 })
  );
  await assertSucceeds(
    setDoc(doc(alice, "users/alice"), { displayName: "A" })
  );
});

test("owner cannot inflate score on update, admin can", async () => {
  await seed("users/alice", { displayName: "A", nexusScore: 10 });
  await assertFails(updateDoc(doc(db("alice"), "users/alice"), {
    nexusScore: 9999,
  }));
  await assertSucceeds(updateDoc(doc(db("admin", { admin: true }), "users/alice"), {
    nexusScore: 9999,
  }));
});

test("anonymous can read public leaderboard profiles", async () => {
  await seed("publicProfiles/alice", { displayName: "Alice", streak: 12 });
  await assertSucceeds(getDoc(doc(anon(), "publicProfiles/alice")));
});

test("publicProfiles rejects client-injected ranking fields", async () => {
  const alice = db("alice");
  await assertFails(
    setDoc(doc(alice, "publicProfiles/alice"), {
      displayName: "Alice",
      nexusScore: 9999,
    })
  );
  await assertSucceeds(
    setDoc(doc(alice, "publicProfiles/alice"), {
      displayName: "Alice",
      streak: 3,
      skills: ["kotlin"],
    })
  );
  await assertFails(
    updateDoc(doc(alice, "publicProfiles/alice"), { rank: 1 })
  );
});

test("public profile publishes leaderboard fields but nothing else", async () => {
  const alice = db("alice");
  await assertSucceeds(
    setDoc(doc(alice, "publicProfiles/alice"), {
      displayName: "Alice",
      college: "IIT Bombay",
      totalXP: 1200,
      streak: 4,
    })
  );
  await assertSucceeds(getDoc(doc(db("bob"), "publicProfiles/alice")));
  await assertFails(
    setDoc(doc(alice, "publicProfiles/alice"), {
      displayName: "Alice",
      totalXP: "9999",
    })
  );
  await assertFails(
    setDoc(doc(db("bob"), "publicProfiles/alice"), {
      displayName: "Bob",
      totalXP: 9999,
    })
  );
  await assertFails(
    updateDoc(doc(alice, "publicProfiles/alice"), { fcmToken: "secret" })
  );
  await assertSucceeds(
    updateDoc(doc(alice, "publicProfiles/alice"), { totalXP: 1300 })
  );
  await assertSucceeds(deleteDoc(doc(alice, "publicProfiles/alice")));
});

test("ai_usage counters are server-only", async () => {
  await seed("ai_usage/alice", { uid: "alice", used: 5 });
  const alice = db("alice");
  await assertSucceeds(getDoc(doc(alice, "ai_usage/alice")));
  await assertFails(getDoc(doc(db("mallory"), "ai_usage/alice")));
  await assertFails(
    setDoc(doc(alice, "ai_usage/alice"), { uid: "alice", used: 0 })
  );
  await assertFails(
    updateDoc(doc(alice, "ai_usage/alice"), { used: 0 })
  );
});

test("ai_proxy_logs cannot be forged but are owner-readable", async () => {
  await seed("ai_proxy_logs/log1", { uid: "alice", tokens: 100 });
  const alice = db("alice");
  await assertSucceeds(getDoc(doc(alice, "ai_proxy_logs/log1")));
  await assertFails(getDoc(doc(db("mallory"), "ai_proxy_logs/log1")));
  await assertFails(
    setDoc(doc(alice, "ai_proxy_logs/log2"), { uid: "alice", tokens: 1 })
  );
  await assertFails(deleteDoc(doc(alice, "ai_proxy_logs/log1")));
});

test("interview outcomes must be anonymized on create", async () => {
  const alice = db("alice");
  await assertFails(
    setDoc(doc(alice, "interview_outcomes/o1"), {
      isAnonymized: false,
      reporterId: "alice",
      company: "Acme",
    })
  );
  await assertSucceeds(
    setDoc(doc(alice, "interview_outcomes/o2"), {
      isAnonymized: true,
      reporterId: "alice",
      company: "Acme",
    })
  );
  await assertSucceeds(getDoc(doc(db("bob"), "interview_outcomes/o2")));
});

test("opportunities are read-only for signed-in users", async () => {
  await seed("opportunities/op1", { title: "Intern", isActive: true });
  await assertFails(getDoc(doc(anon(), "opportunities/op1")));
  await assertSucceeds(getDoc(doc(db("alice"), "opportunities/op1")));
  await assertFails(
    setDoc(doc(db("alice"), "opportunities/op2"), { title: "Fake" })
  );
});

test("unknown collections are denied by the catch-all", async () => {
  const alice = db("alice");
  await assertFails(
    setDoc(doc(alice, "arbitrary/thing"), { hello: "world" })
  );
  await assertFails(getDoc(doc(anon(), "arbitrary/thing")));
});

test("leetcode stats cache is server-only", async () => {
  await seed("leetcode_stats/tourist", {
    username: "tourist",
    stats: { totalSolved: 1 },
    fetchedAt: 0,
  });
  await assertFails(getDoc(doc(db("alice"), "leetcode_stats/tourist")));
  await assertFails(
    setDoc(doc(db("alice"), "leetcode_stats/tourist"), { username: "spoof" })
  );
});
