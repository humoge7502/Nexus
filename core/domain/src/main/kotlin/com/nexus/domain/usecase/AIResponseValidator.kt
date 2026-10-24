package com.nexus.domain.usecase

import javax.inject.Inject
import javax.inject.Singleton

data class AIWarning(
    val type: WarningType,
    val message: String,
    val severity: WarningSeverity,
)

enum class WarningType {
    SALARY_CLAIM, FALSE_CERTAINTY, COMPANY_CLAIM, OUTDATED_ADVICE, HARMFUL_ADVICE,
    MEDICAL_ADVICE, FINANCIAL_ADVICE, PERSONAL_INFO,
}

enum class WarningSeverity { NONE, INFO, MEDIUM, HIGH }

data class ValidatedAIResponse(
    val content: String,
    val warnings: List<AIWarning>,
    val isClean: Boolean,
    val highestSeverity: WarningSeverity = WarningSeverity.NONE,
)

@Singleton
class AIResponseValidator @Inject constructor() {

    fun validate(response: String): ValidatedAIResponse {
        val warnings = mutableListOf<AIWarning>()

        val salaryPatterns = listOf(
            Regex("""₹\s*[\d,.]+\s*(LPA|lpa|CTC|ctc|lakhs?|crore)"""),
            Regex("""\d+\s*(LPA|lpa|CTC|ctc|lakhs?\s+per\s+annum)"""),
            Regex("""salary\s+(?:of|is|around|approximately)\s+₹?\s*[\d,.]+""", RegexOption.IGNORE_CASE),
        )
        if (salaryPatterns.any { it.containsMatchIn(response) }) {
            warnings.add(AIWarning(
                type = WarningType.SALARY_CLAIM,
                message = "Salary figures are estimates. Verify with current job postings.",
                severity = WarningSeverity.INFO,
            ))
        }

        val guaranteePatterns = listOf(
            Regex("""\b(guaranteed|definitely\s+will|100%\s+chance|certainly\s+will|always\s+get)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(you\s+will\s+get|you\s+are\s+sure\s+to)\b""", RegexOption.IGNORE_CASE),
        )
        if (guaranteePatterns.any { it.containsMatchIn(response) }) {
            warnings.add(AIWarning(
                type = WarningType.FALSE_CERTAINTY,
                message = "Career outcomes depend on many factors and cannot be guaranteed.",
                severity = WarningSeverity.MEDIUM,
            ))
        }

        val companyClaimPattern = Regex(
            """\b(Google|Microsoft|Amazon|Meta|Apple|Flipkart|Infosys|TCS|Wipro|HCL)\s+(always|never|only\s+hires?|requires?|doesn't|won't)\b""",
            RegexOption.IGNORE_CASE,
        )
        if (companyClaimPattern.containsMatchIn(response)) {
            warnings.add(AIWarning(
                type = WarningType.COMPANY_CLAIM,
                message = "Company practices change frequently. Verify with official sources or recent hires.",
                severity = WarningSeverity.INFO,
            ))
        }

        val outdatedTechPatterns = listOf(
            Regex("""\b(jQuery\s+is\s+(?:the\s+)?(?:best|recommended|essential))\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(Java\s+is\s+(?:dead|dying|obsolete))\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(you\s+(?:must|need\s+to)\s+learn\s+(?:COBOL|Fortran|Pascal))\b""", RegexOption.IGNORE_CASE),
        )
        if (outdatedTechPatterns.any { it.containsMatchIn(response) }) {
            warnings.add(AIWarning(
                type = WarningType.OUTDATED_ADVICE,
                message = "Technology relevance changes rapidly. Cross-check with current job listings.",
                severity = WarningSeverity.MEDIUM,
            ))
        }

        val medicalPatterns = listOf(
            Regex("""\b(medication|medicine|dosage|prescription|diagnos[ei]s|treat\s+your\s+condition|stop\s+taking)\b""", RegexOption.IGNORE_CASE),
        )
        if (medicalPatterns.any { it.containsMatchIn(response) }) {
            warnings.add(AIWarning(
                type = WarningType.MEDICAL_ADVICE,
                message = "I'm an AI career mentor, not a medical professional. Please consult a doctor for medical concerns.",
                severity = WarningSeverity.HIGH,
            ))
        }

        val financialPatterns = listOf(
            Regex("""\b(invest|buy\s+stocks?|shares?\s+of|crypto|bitcoin|trading\s+tip|mutual\s+fund)\b""", RegexOption.IGNORE_CASE),
        )
        if (financialPatterns.any { it.containsMatchIn(response) }) {
            warnings.add(AIWarning(
                type = WarningType.FINANCIAL_ADVICE,
                message = "This is not financial advice. Consult a certified financial advisor before investing.",
                severity = WarningSeverity.MEDIUM,
            ))
        }

        val personalInfoPatterns = listOf(
            Regex("""\b\d{4}[\s-]?\d{4}[\s-]?\d{4}[\s-]?\d{4}\b"""),
            Regex("""\b\d{3}-\d{2}-\d{4}\b"""),
            Regex("""\b\d{4}\s\d{4}\s\d{4}\b"""),
            Regex("""(?i)\b\d+\s+\w+\s+(street|st|road|rd|avenue|ave|lane|ln)\b"""),
        )
        if (personalInfoPatterns.any { it.containsMatchIn(response) }) {
            warnings.add(AIWarning(
                type = WarningType.PERSONAL_INFO,
                message = "Never share personal or financial identifiers in chat. Remove them before storing or sharing.",
                severity = WarningSeverity.MEDIUM,
            ))
        }

        val harmfulPatterns = listOf(
            Regex("""\b(drop\s+out|quit\s+college|don't\s+(?:need|bother\s+with)\s+(?:a\s+)?degree)\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(lie\s+(?:on|in)\s+(?:your\s+)?resume|fake\s+(?:your\s+)?experience)\b""", RegexOption.IGNORE_CASE),
        )
        if (harmfulPatterns.any { it.containsMatchIn(response) }) {
            warnings.add(AIWarning(
                type = WarningType.HARMFUL_ADVICE,
                message = "This advice may be harmful. Please consult a career counselor.",
                severity = WarningSeverity.HIGH,
            ))
        }

        return ValidatedAIResponse(
            content = response.take(4000),
            warnings = warnings,
            isClean = warnings.isEmpty(),
            highestSeverity = warnings.maxByOrNull { it.severity.ordinal }?.severity ?: WarningSeverity.NONE,
        )
    }
}
