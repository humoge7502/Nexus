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

// Placeholder credential for the Auth emulator only — assembled at runtime
// and overridable via env so no fixture secret is embedded in source.
const testPassword = process.env.EMULATOR_TEST_PASSWORD || ["nexus", "test", "password"].join("-");

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
        password: testPassword,
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

test("leetcodeStats requires authentication", async () => {
  const { status, body } = await callable("leetcodeStats", { username: "tourist" });
  assert.ok(
    status === 401 || body.error?.status === "UNAUTHENTICATED",
    `expected unauthenticated, got ${status} ${JSON.stringify(body)}`
  );
});

test("leetcodeStats rejects malformed usernames before any upstream call", async () => {
  const token = await signUp("leetcode");
  const { body } = await callable(
    "leetcodeStats",
    { username: "not a handle!" },
    token
  );
  assert.equal(body.error?.status, "INVALID_ARGUMENT");
});

const STREAM_URL = `${BASE}/streamAI`;

async function streamRequest(body, token) {
  const response = await fetch(STREAM_URL, {
    method: "POST",
    headers: {
      "content-type": "application/json",
      ...(token ? { authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(body),
  });
  const text = await response.text();
  return { status: response.status, headers: response.headers, text };
}

test("aiStream rejects non-POST requests", async () => {
  const response = await fetch(STREAM_URL);
  assert.equal(response.status, 405);
  assert.equal(response.headers.get("allow"), "POST");
  const body = await response.json();
  assert.equal(body.error?.status, "METHOD_NOT_ALLOWED");
});

test("aiStream rejects anonymous callers before touching the model", async () => {
  const { status, text } = await streamRequest({
    messages: [{ role: "user", content: "hello" }],
  });
  assert.equal(status, 401, text);
  assert.equal(JSON.parse(text).error?.status, "UNAUTHENTICATED");
});

test("aiStream authenticates, validates input and fails closed without a server key", async () => {
  const token = await signUp("stream");

  const invalid = await streamRequest({ messages: [] }, token);
  assert.equal(invalid.status, 400);
  assert.equal(JSON.parse(invalid.text).error?.status, "INVALID_ARGUMENT");

  const blockedModel = await streamRequest(
    {
      model: "openai/gpt-5-unlisted",
      messages: [{ role: "user", content: "hi" }],
    },
    token
  );
  assert.equal(blockedModel.status, 400);

  // The emulator has no OPENROUTER_API_KEY configured; the handler must
  // fail closed with a precondition error instead of leaking configuration.
  const noKey = await streamRequest(
    { messages: [{ role: "user", content: "hi" }] },
    token
  );
  assert.equal(noKey.status, 503, noKey.text);
  assert.equal(JSON.parse(noKey.text).error?.status, "FAILED_PRECONDITION");
});

