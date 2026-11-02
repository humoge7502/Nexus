import test from "node:test";
import assert from "node:assert/strict";
import { deriveSkills } from "../src/earlyCareer.mjs";
import { isTechnicalUnstopRole, unstopLocation, unstopStipendLabel } from "../src/sources.mjs";

test("UI/UX is not claimed from mechanical product design", () => {
  assert.deepEqual(
    deriveSkills("Work on 3D modelling, product design, and mechanical component development"),
    [],
  );
  assert.deepEqual(deriveSkills("Design screens in Figma and hand off a prototype"), ["UI/UX"]);
});

test("a technical role is judged on the role, not the company blurb", () => {
  assert.equal(
    isTechnicalUnstopRole({
      title: "Business Development Internship",
      workFunctions: ["B2B Sales", "Business Development"],
    }),
    false,
  );
  assert.equal(
    isTechnicalUnstopRole({ title: "Data Entry Internship", workFunctions: ["Operations"] }),
    false,
  );
  assert.equal(
    isTechnicalUnstopRole({ title: "React Native Internship", workFunctions: ["Frontend Development"] }),
    true,
  );
  assert.equal(isTechnicalUnstopRole({ title: "Intern", workFunctions: ["Data Science"] }), true);
});

test("stipend is stated only as precisely as the listing states it", () => {
  assert.equal(unstopStipendLabel(null), "Stipend not listed");
  assert.equal(unstopStipendLabel({ not_disclosed: true, min_salary: 20000 }), "Stipend not listed");
  assert.equal(unstopStipendLabel({ paid_unpaid: "paid", min_salary: null }), "Paid internship");
  assert.equal(unstopStipendLabel({ paid_unpaid: "unpaid" }), "Unpaid internship");
  assert.equal(
    unstopStipendLabel({ paid_unpaid: "paid", min_salary: 5000, max_salary: 12000, currency: "fa-rupee", pay_in: "monthly" }),
    "₹5,000-₹12,000/month",
  );
  assert.equal(
    unstopStipendLabel({ paid_unpaid: "paid", min_salary: 3000, max_salary: 3000, currency: "fa-rupee", pay_in: "monthly" }),
    "₹3,000/month",
  );
});

test("work-from-home is claimed only when the listing says the work is from home", () => {
  assert.equal(
    unstopLocation({
      region: "online",
      jobDetail: { type: "in_office" },
      locations: [{ city: "Bangalore", state: "Karnataka", country: "India" }],
    }),
    "Bangalore, Karnataka, India",
  );
  assert.equal(
    unstopLocation({
      region: "online",
      jobDetail: { type: "wfh" },
      locations: [{ city: "Noida", state: "Uttar Pradesh", country: "India" }],
    }),
    "Noida, Uttar Pradesh, India / WFH",
  );
  assert.equal(unstopLocation({ region: "online", jobDetail: { type: "wfh" }, locations: [] }), "Remote");
  assert.equal(unstopLocation({ region: "offline", jobDetail: { type: "in_office" }, locations: [] }), "");
});
