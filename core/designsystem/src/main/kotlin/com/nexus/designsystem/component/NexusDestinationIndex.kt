package com.nexus.designsystem.component

/**
 * Ranking for [NexusCommandPalette]. The palette owns presentation; this owns
 * only the question "which commands answer what was typed".
 *
 * A command matches on its title, subtitle, category and aliases. Ranking is
 * deliberately simple and predictable — prefix beats contains, title beats
 * alias, alias beats category — because a jump-list must put the obvious
 * answer first, and a fuzzy score that surprises the user is worse than no
 * score at all. Ties keep their incoming order so the caller's curation of
 * the list is never silently reshuffled.
 */
object NexusDestinationIndex {

    private const val TITLE_PREFIX = 100
    private const val WORD_PREFIX = 80
    private const val TITLE_CONTAINS = 60
    private const val ALIAS_PREFIX = 50
    private const val ALIAS_CONTAINS = 40
    private const val CATEGORY_CONTAINS = 20
    private const val SUBTITLE_CONTAINS = 10

    fun resolveCommands(query: String, commands: List<NexusCommand>): List<NexusCommand> {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return commands

        return commands
            .mapIndexedNotNull { index, command ->
                val score = scoreOf(needle, command)
                if (score <= 0) null else Ranked(index, score, command)
            }
            .sortedWith(compareByDescending<Ranked> { it.score }.thenBy { it.index })
            .map { it.command }
    }

    private fun scoreOf(needle: String, command: NexusCommand): Int {
        val title = command.title.lowercase()
        val best = when {
            title.startsWith(needle) -> TITLE_PREFIX
            title.split(' ', '-', '/', '&').any { it.startsWith(needle) } -> WORD_PREFIX
            title.contains(needle) -> TITLE_CONTAINS
            else -> 0
        }
        if (best > 0) return best

        val aliasBest = command.aliases
            .map { it.lowercase() }
            .maxOfOrNull { alias ->
                when {
                    alias.startsWith(needle) -> ALIAS_PREFIX
                    alias.contains(needle) -> ALIAS_CONTAINS
                    else -> 0
                }
            } ?: 0
        if (aliasBest > 0) return aliasBest

        if (command.category.lowercase().contains(needle)) return CATEGORY_CONTAINS
        if (command.subtitle?.lowercase()?.contains(needle) == true) return SUBTITLE_CONTAINS
        return 0
    }

    private data class Ranked(
        val index: Int,
        val score: Int,
        val command: NexusCommand,
    )
}