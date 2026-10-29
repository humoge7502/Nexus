package com.nexus.audit.rules

import com.nexus.audit.core.types.Severity

/**
 * Security rules (Phase 6).
 */
object SecurityRules {
    val HARDCODED_SECRET = Rule(
        id = "SEC-001",
        title = "Potential hardcoded secret in source",
        category = "Security",
        severity = Severity.CRITICAL,
        description = "A string literal that looks like an API key/token/password was found in source.",
        remediation = "Move secrets to backend/proxy or Android Keystore; never ship in the APK."
    )

    val SSL_BYPASS = Rule(
        id = "SEC-002",
        title = "TLS/SSL verification disabled",
        category = "Security",
        severity = Severity.CRITICAL,
        description = "Code disables certificate or hostname verification, exposing traffic to MITM.",
        remediation = "Remove the bypass; pin certificates if needed via CertificatePinner."
    )

    val INSECURE_ENDPOINT = Rule(
        id = "SEC-003",
        title = "Plaintext HTTP endpoint referenced",
        category = "Security",
        severity = Severity.HIGH,
        description = "An http:// (non-TLS) endpoint is referenced; traffic can be intercepted.",
        remediation = "Use https:// for all endpoints."
    )

    val UNRELIABLE_THIRD_PARTY = Rule(
        id = "SEC-004",
        title = "Dependency on an unreliable third-party host",
        category = "Security",
        severity = Severity.HIGH,
        description = "A free/community-hosted API (e.g. onrender.com) is a single point of failure " +
            "and an availability/trust risk.",
        remediation = "Cache responses with a TTL, add a fallback source, and degrade gracefully."
    )

    val ALLOW_BACKUP = Rule(
        id = "SEC-005",
        title = "allowBackup enabled without device-to-device export controls",
        category = "Security",
        severity = Severity.MEDIUM,
        description = "android:allowBackup=true lets ADB/cloud backup extract app data including tokens.",
        remediation = "Set allowBackup=false or define backup rules; store secrets in AndroidKeyStore."
    )

    val KEY_RELEASE_SAFE = Rule(
        id = "SEC-006",
        title = "AI key only used in debug direct path (release forces proxy)",
        category = "Security",
        severity = Severity.INFO,
        description = "Release builds route AI calls through a backend proxy; the OpenRouter key is " +
            "never bundled in release artifacts.",
        remediation = "Keep this invariant; add a CI assertion that the release APK contains no key."
    )

    init {
        listOf(HARDCODED_SECRET, SSL_BYPASS, INSECURE_ENDPOINT, UNRELIABLE_THIRD_PARTY,
            ALLOW_BACKUP, KEY_RELEASE_SAFE).forEach(RuleCatalog::register)
    }
}
