"use strict";

/**
 * End-to-end callable-function tests against the Firebase emulators.
 * Run with: npm run test:emulator
 * (wraps this file in `firebase emulators:exec --only firestore,functions,auth`).
 */

const { test } = require("node:test");
const assert = require("node:assert/strict");

const PROJECT = "demo-nexus";
const FUNCTIONS_HOST = process.env.FUNCTIONS_EMULATOR_HOST || "127.0.0.1:5001";
const AUTH_HOST = process.env.FIREBASE_AUTH_EMULATOR_HOST || "127.0.0.1:9099";
const BASE = `http://${FUNCTIONS_HOST}/${PROJECT}/us-central1`;

async function callable(name, data, token) {
  const response = await fetch(`${BASE}/${name}`, {
    method: "POST",
    headers: {
      "content-type": "application/json",
      ...(token ? { authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({ data }),
  });
  const body = await response.json().catch(() => ({}));
  return { status: response.status, body };
}

async function signUp(suffix) {
  const email = `emulator_${suffix}_${Date.now()}@example.com`;
  const response = await fetch(
    `http://${AUTH_HOST}/identitytoolkit.googleapis.com/v1/accounts:signUp?key=fake-api-key`,
    {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        email,
        password: "nexus-test-password",
        returnSecureToken: true,
      }),
    }
  );
  const text = await response.text();
  assert.equal(response.status, 200, `signUp failed: ${text}`);
  const json = JSON.parse(text);
  assert.ok(json.idToken, "auth emulator returned no idToken");
  return json.idToken;
}

test("health callable responds", async () => {
  const { status, body } = await callable("health", {});
  assert.equal(status, 200);
  assert.equal(body.result?.status, "ok");
});

test("proxyAI rejects anonymous callers", async () => {
  const { status, body } = await callable("proxyAI", {
    messages: [{ role: "user", content: "hello" }],
  });
  assert.ok(
    status === 401 || body.error?.status === "UNAUTHENTICATED",
    `expected unauthenticated, got ${status} ${JSON.stringify(body)}`
  );
});

test("proxyAI authenticates, enforces validation and fails closed without a server key", async () => {
  const token = await signUp("proxy");

  const invalid = await callable(
    "proxyAI",
    { messages: [] },
    token
  );
  assert.equal(invalid.body.error?.status, "INVALID_ARGUMENT");

  const blockedModel = await callable(
    "proxyAI",
    { model: "openai/gpt-5-unlisted", messages: [{ role: "user", content: "hi" }] },
    token
  );
  assert.equal(blockedModel.body.error?.status, "INVALID_ARGUMENT");

  // No OPENROUTER_API_KEY is configured for the emulator, so the handler must
  // fail closed with a precondition error rather than attempting an upstream
  // call or leaking configuration details.
  const { body } = await callable(
    "proxyAI",
    { messages: [{ role: "user", content: "hi" }] },
    token
  );
  assert.equal(body.error?.status, "FAILED_PRECONDITION");
  assert.match(body.error?.message ?? "", /not configured/i);
});

test("opportunity feed requires authentication", async () => {
  const { status, body } = await callable("getOpportunityFeed", { limit: 5 });
  assert.ok(
    status === 401 || body.error?.status === "UNAUTHENTICATED",
    `expected unauthenticated, got ${status} ${JSON.stringify(body)}`
  );
});

test("opportunity feed returns a bounded page for authenticated users", async () => {
  const token = await signUp("feed");
  const { status, body } = await callable(
    "getOpportunityFeed",
    { opportunityType: "all", limit: 999, offset: -5 },
    token
  );
  assert.equal(status, 200, JSON.stringify(body));
  assert.ok(Array.isArray(body.result?.results), "missing results array");
  assert.ok(body.result.results.length <= 50);
});
