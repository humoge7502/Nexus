import * as admin from "firebase-admin";
import { healthCheck } from "./health";
import { aiProxy } from "./ai/proxy";
import { deepResearch } from "./ai/research";
import { opportunityFeed } from "./opportunities/feed";
import { intelligenceOutcomes } from "./intelligence/outcomes";
import { cohortIntelligence } from "./intelligence/cohort";

admin.initializeApp();

// Each handler module already exports a fully-built HttpsFunction
// (functions.https.onCall(...)). Export them directly — wrapping an
// HttpsFunction in onCall() again passes the object as a handler and
// breaks the deployment.
export const health = healthCheck;
export const proxyAI = aiProxy;
export const deepResearchFn = deepResearch;
export const getOpportunityFeed = opportunityFeed;
export const getIntelligenceOutcomes = intelligenceOutcomes;
export const getCohortIntelligence = cohortIntelligence;
