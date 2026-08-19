package dev.gaphunter.regexnamedgroupcompanion.match

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Pure `java.util.regex` group-name parsing, no Swing, no PSI -- fully
 * testable in isolation, same split as [RegexMatcher]/`RegexOptions` in
 * Regex Preview Companion (this plugin extends that engine, doesn't
 * replace it).
 *
 * [Pattern.namedGroups] (public API since JDK 20, and the IDE's own
 * bundled runtime is JDK 21+ for every `sinceBuild` this plugin
 * targets) exists but returns an unordered `Map<String, Int>` -- a real
 * finding from a live check, not an assumption (`(?<z>a)(?<a>b)(?<m>c)`
 * came back as `{z=1, a=2, m=3}` by group-index order in one JDK build
 * but is NOT a documented contract, and other collision orders are
 * possible). Source order is what both the inlay hint and the tool
 * window need (a group must appear in the same left-to-right order the
 * developer wrote it), so this scans the pattern *text* itself instead
 * of trusting the Map's iteration order.
 */
object NamedGroupExtractor {

    // Matches (?<name> -- deliberately excludes (?<= and (?<! (lookbehind),
    // which use the same `(?<` prefix but are never named groups.
    private val NAMED_GROUP_HEADER = Regex("""\(\?<([a-zA-Z][a-zA-Z0-9]*)>""")

    /**
     * Names of every named group in [patternText], in the order they
     * appear in the pattern source. Returns an empty list for a pattern
     * with no named groups (not an error) and for an unparsable pattern
     * (callers that need the real compile error should go through
     * [NamedGroupMatcher] instead, which surfaces
     * [PatternSyntaxException] properly).
     */
    fun groupNamesInOrder(patternText: String): List<String> {
        if (patternText.isEmpty()) return emptyList()
        return NAMED_GROUP_HEADER.findAll(patternText).map { it.groupValues[1] }.distinct().toList()
    }

    /** True only when [patternText] both compiles and has at least one named group -- the exact gate the inlay hint uses to decide whether to render anything on a given literal. */
    fun hasNamedGroups(patternText: String): Boolean {
        if (groupNamesInOrder(patternText).isEmpty()) return false
        return try {
            Pattern.compile(patternText)
            true
        } catch (_: PatternSyntaxException) {
            false
        }
    }
}
