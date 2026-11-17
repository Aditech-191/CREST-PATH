const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_mak_123";
const BOB_UID = "bob_kyu_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validProfilePayload(uid, ts = new Date(Date.now() - 5000)) {
  return {
    userId: uid,
    fullName: "Amina Nakato",
    university: "Makerere University",
    program: "BSc Computer Science",
    yearOfStudy: 3,
    gpa: "4.42",
    skills: ["Kotlin", "Android", "Python"],
    interests: ["FinTech", "Mobile Apps"],
    activeDemoProfileId: "",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validApplicationPayload(uid, oppId, ts = new Date(Date.now() - 5000)) {
  return {
    userId: uid,
    opportunityId: oppId,
    saved: true,
    status: "SAVED",
    notes: "Prepare Makerere transcript and CV.",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validCustomOppPayload(uid, ts = new Date(Date.now() - 5000)) {
  return {
    userId: uid,
    title: "Kampala FinTech Mobile Intern",
    organization: "EastPay Labs",
    category: "INTERNSHIP",
    location: "Kampala, Uganda (Hybrid)",
    deadlineText: "15 Dec 2026",
    deadlineIso: "2026-12-15",
    stipendOrValue: "UGX 800,000 / month",
    eligibility: "Year 2-4 Computer Science or Software Engineering students.",
    requirements: ["Updated CV", "GitHub portfolio"],
    requiredSkills: ["Kotlin", "Android", "Git"],
    officialUrl: "",
    summary: "6-month industrial training placement in mobile payments.",
    verificationWarnings: ["No official company domain link found in forward."],
    verified: false,
    createdAt: ts,
    updatedAt: ts,
  };
}

test("Unauthenticated users cannot read or write profiles, applications, or custom opportunities", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("applications").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("custom_opportunities").get()
  );
});

test("Student profile: owner create, partial profile update, demo switch, and shadow/tamper rejections", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created))
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Partial update: demo profile switch
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      activeDemoProfileId: "demo_grace",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Partial update: profile details
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      fullName: "Amina N. K.",
      skills: ["Kotlin", "Jetpack Compose", "SQL"],
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost/shadow field injection
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject immutable createdAt tamper
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 200),
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Reject future timestamp tamper
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      activeDemoProfileId: "demo_amina",
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Reject listing /users root collection (PII protection)
  await assertFails(aliceDb.collection("users").get());
});

test("Cross-user isolation: Bob cannot read or modify Alice's profile, applications, or custom opportunities", async () => {
  const created = new Date(Date.now() - 10000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = ctx.firestore();
    await adminDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created));
    await adminDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("applications")
      .doc("mtn_ug_intern_2026")
      .set(validApplicationPayload(ALICE_UID, "mtn_ug_intern_2026", created));
    await adminDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("custom_opportunities")
      .doc("custom_opp_1")
      .set(validCustomOppPayload(ALICE_UID, created));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({ fullName: "Hijacked" })
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("applications").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("applications")
      .doc("mtn_ug_intern_2026")
      .update({ status: "APPLIED" })
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("custom_opportunities").get()
  );
});

test("Applications subcollection: owner list, create, status/notes update, and invalid enum/id rejection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Empty subcollection list must succeed
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("applications").get()
  );

  // Create tracker doc
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("applications")
      .doc("mtn_ug_intern_2026")
      .set(validApplicationPayload(ALICE_UID, "mtn_ug_intern_2026", created))
  );

  // Partial update: mark as APPLIED with appliedAtIso
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("applications")
      .doc("mtn_ug_intern_2026")
      .update({
        saved: true,
        status: "APPLIED",
        appliedAtIso: "2026-11-17",
        notes: "Submitted online via MTN portal.",
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Reject invalid status enum
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("applications")
      .doc("mtn_ug_intern_2026")
      .update({
        status: "HIRED_BY_MAGIC",
        updatedAt: new Date(Date.now() - 500),
      })
  );

  // Reject mismatched @DocumentId 'id' key on create
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("applications")
      .doc("stanbic_ug_2026")
      .set({
        id: "stanbic_ug_2026",
        ...validApplicationPayload(ALICE_UID, "stanbic_ug_2026", created),
      })
  );
});

test("Custom opportunities subcollection: owner CRUD succeeds and verified:true spoofing is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Empty subcollection list must succeed
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("custom_opportunities").get()
  );

  // Create valid unverified forward
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("custom_opportunities")
      .doc("custom_opp_1")
      .set(validCustomOppPayload(ALICE_UID, created))
  );

  // Reject verified: true spoofing on create
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("custom_opportunities")
      .doc("custom_opp_spoof")
      .set({
        ...validCustomOppPayload(ALICE_UID, created),
        verified: true,
      })
  );

  // Reject verified: true spoofing on update
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("custom_opportunities")
      .doc("custom_opp_1")
      .update({
        verified: true,
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Owner delete succeeds
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("custom_opportunities")
      .doc("custom_opp_1")
      .delete()
  );
});
