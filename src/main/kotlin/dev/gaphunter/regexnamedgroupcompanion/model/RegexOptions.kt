package dev.gaphunter.regexnamedgroupcompanion.model

import java.util.regex.Pattern

data class RegexOptions(
    val caseInsensitive: Boolean = false,
    val multiline: Boolean = false,
    val dotAll: Boolean = false,
) {
    fun toJavaFlags(): Int {
        var flags = 0
        if (caseInsensitive) flags = flags or Pattern.CASE_INSENSITIVE
        if (multiline) flags = flags or Pattern.MULTILINE
        if (dotAll) flags = flags or Pattern.DOTALL
        return flags
    }
}

data class MatchRange(val start: Int, val end: Int)

/** One named group's value for one specific match -- null when that group didn't participate in this match (e.g. inside an alternation branch that wasn't taken). */
data class NamedGroupValue(val name: String, val value: String?)

/** One match, with its full range plus every named group's extracted value for that same match. */
data class NamedGroupMatch(val range: MatchRange, val groups: List<NamedGroupValue>)

sealed class RegexPreviewResult {
    data class Success(val matches: List<MatchRange>) : RegexPreviewResult()
    data class Error(val message: String) : RegexPreviewResult()
}

/** Same shape as [RegexPreviewResult] but each match also carries its named-group extractions -- what the tool window's "Named Groups" section renders. */
sealed class NamedGroupPreviewResult {
    data class Success(val matches: List<NamedGroupMatch>) : NamedGroupPreviewResult()
    data class Error(val message: String) : NamedGroupPreviewResult()
}
