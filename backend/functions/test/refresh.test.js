"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");

const { isExpired, decideExpiries } = require("../lib/opportunities/refresh");

const NOW = Date.parse("2026-10-30T06:00:00.000Z");

test("isExpired: epoch-millis deadline in the past is expired", () => {
  assert.equal(isExpired(NOW - 1000, NOW), true);
});

test("isExpired: future epoch deadline stays active", () => {
  assert.equal(isExpired(NOW + 86_400_000, NOW), false);
});

test("isExpired: ISO string deadline is parsed and compared", () => {
  assert.equal(isExpired("2026-10-29T00:00:00.000Z", NOW), true);
  assert.equal(isExpired("2026-11-15T00:00:00.000Z", NOW), false);
});

test("isExpired: null or missing deadline never expires", () => {
  assert.equal(isExpired(null, NOW), false);
  assert.equal(isExpired(undefined, NOW), false);
});

test("decideExpiries: only active-and-expired docs are deactivated", () => {
  const decision = decideExpiries(
    [
      { id: "a", deadline: NOW - 1, isActive: true },
      { id: "b", deadline: NOW + 1, isActive: true },
      { id: "c", deadline: NOW - 1, isActive: false }, // already inactive
      { id: "d", deadline: null, isActive: true }, // rolling listing
    ],
    NOW
  );
  assert.deepEqual(decision.toDeactivate, ["a"]);
  assert.equal(decision.keptActive, 2);
});

test("decideExpiries: empty input is a no-op", () => {
  const decision = decideExpiries([], NOW);
  assert.deepEqual(decision.toDeactivate, []);
  assert.equal(decision.keptActive, 0);
});
