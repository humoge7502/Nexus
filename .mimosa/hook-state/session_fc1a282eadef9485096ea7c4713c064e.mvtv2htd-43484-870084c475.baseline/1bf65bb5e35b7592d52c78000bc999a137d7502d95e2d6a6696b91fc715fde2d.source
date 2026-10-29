"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");

const {
  bearerToken,
  statusForHttpsError,
  lastReportedTokens,
} = require("../lib/ai/stream");

test("bearerToken parses well-formed Authorization headers", () => {
  assert.equal(bearerToken("Bearer abc.def.ghi"), "abc.def.ghi");
  assert.equal(bearerToken("bearer   token-value  "), "token-value");
});

test("bearerToken rejects missing or malformed headers", () => {
  assert.equal(bearerToken(undefined), null);
  assert.equal(bearerToken(""), null);
  assert.equal(bearerToken("Basic abc"), null);
  assert.equal(bearerToken("Bearer"), null);
  assert.equal(bearerToken("Bearer   "), null);
});

test("statusForHttpsError maps quota and validation failures", () => {
  assert.deepEqual(statusForHttpsError("resource-exhausted"), {
    httpStatus: 429,
    errorStatus: "RESOURCE_EXHAUSTED",
  });
  assert.deepEqual(statusForHttpsError("invalid-argument"), {
    httpStatus: 400,
    errorStatus: "INVALID_ARGUMENT",
  });
  assert.deepEqual(statusForHttpsError("unauthenticated"), {
    httpStatus: 401,
    errorStatus: "UNAUTHENTICATED",
  });
  assert.deepEqual(statusForHttpsError("failed-precondition"), {
    httpStatus: 503,
    errorStatus: "FAILED_PRECONDITION",
  });
  assert.deepEqual(statusForHttpsError("internal"), {
    httpStatus: 502,
    errorStatus: "UNAVAILABLE",
  });
});

test("lastReportedTokens keeps the final usage chunk", () => {
  const stream = [
    'data: {"choices":[{"delta":{"content":"hi"}}]}',
    'data: {"choices":[],"usage":{"prompt_tokens":10,"completion_tokens":5,"total_tokens":15}}',
    "data: [DONE]",
  ].join("\n\n");
  assert.equal(lastReportedTokens(stream), 15);
});

test("lastReportedTokens returns 0 when usage is absent", () => {
  assert.equal(lastReportedTokens("data: [DONE]"), 0);
  assert.equal(lastReportedTokens(""), 0);
});
