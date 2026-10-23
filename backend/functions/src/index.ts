import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { healthCheck } from "./health";
import { aiProxy } from "./ai/proxy";
import { opportunityFeed } from "./opportunities/feed";
import { intelligenceOutcomes } from "./intelligence/outcomes";
import { cohortIntelligence } from "./intelligence/cohort";

admin.initializeApp();

export const health = functions.https.onCall(healthCheck);
export const proxyAI = functions.https.onCall(aiProxy);
export const getOpportunityFeed = functions.https.onCall(opportunityFeed);
export const getIntelligenceOutcomes = functions.https.onCall(intelligenceOutcomes);
export const getCohortIntelligence = functions.https.onCall(cohortIntelligence);
