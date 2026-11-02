package com.nexus.domain.usecase

/**
 * Compares skill names the way a student writes them against the way a posting
 * states them. "React.js", "ReactJS" and "React" are one skill; "Java" and
 * "JavaScript" are not. Feed sources mix all of these, and an exact string
 * comparison silently costs a student both the fit percentage and the alert.
 *
 * Deliberately conservative: abbreviations, punctuation and trailing qualifier
 * words are folded; related technologies are not. "PostgreSQL" is not "SQL", so
 * a listing that wants the former does not claim the student is ready for it.
 * Relatedness belongs to the skill graph, which has edges for it.
 */
object SkillNames {

    private val PARENTHETICAL = Regex("\\([^)]*\\)")
    private val SEPARATORS = Regex("[^a-z0-9+#]+")

    /**
     * Trailing qualifier words that name the same skill as the head word alone.
     * "Design" and "Engineering" are absent on purpose: Product Design and
     * Software Engineering mean something their head word alone does not.
     */
    private val QUALIFIERS = setOf("development", "programming")

    /** Keyed on the joined canonical form, so "react" + "js" folds to "react". */
    private val ALIASES = mapOf(
        "ml" to "machinelearning",
        "dl" to "deeplearning",
        "js" to "javascript",
        "ts" to "typescript",
        "reactjs" to "react",
        "node" to "nodejs",
        "next" to "nextjs",
        "k8s" to "kubernetes",
        "gcp" to "googlecloudplatform",
        "html5" to "html",
        "ai" to "artificialintelligence",
        "ds" to "datascience",
        "ui" to "uiux",
        "ux" to "uiux",
        "uiuxdesign" to "uiux",
        "uidesign" to "uiux",
        "uxdesign" to "uiux",
    )

    fun canonical(raw: String): String {
        val tokens = raw.lowercase()
            .replace(PARENTHETICAL, " ")
            .split(SEPARATORS)
            .filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return ""
        val trimmed = if (
            tokens.size > 1 && tokens.last() in QUALIFIERS && tokens.first() != tokens.last()
        ) {
            tokens.dropLast(1)
        } else {
            tokens
        }
        val joined = trimmed.joinToString(separator = "")
        return ALIASES[joined] ?: joined
    }

    /** True when both sides name the same skill, ignoring how either was written. */
    fun same(required: String, owned: String): Boolean {
        val canonical = canonical(required)
        return canonical.isNotEmpty() && canonical == canonical(owned)
    }

    /** Canonical form of every claim the student made, for comparing against many listings at once. */
    fun canonicalSet(names: Collection<String>): Set<String> =
        names.map { canonical(it) }.filter { it.isNotEmpty() }.toSet()
}
