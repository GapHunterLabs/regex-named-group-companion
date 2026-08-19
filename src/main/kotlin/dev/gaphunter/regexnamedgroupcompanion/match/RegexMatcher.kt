package dev.gaphunter.regexnamedgroupcompanion.match

import dev.gaphunter.regexnamedgroupcompanion.model.MatchRange
import dev.gaphunter.regexnamedgroupcompanion.model.NamedGroupMatch
import dev.gaphunter.regexnamedgroupcompanion.model.NamedGroupPreviewResult
import dev.gaphunter.regexnamedgroupcompanion.model.NamedGroupValue
import dev.gaphunter.regexnamedgroupcompanion.model.RegexOptions
import dev.gaphunter.regexnamedgroupcompanion.model.RegexPreviewResult
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Ported from Regex Preview Companion's `RegexMatcher` -- same
 * compile+match+flags handling, same honest [PatternSyntaxException]
 * surfacing (never a generic "invalid" message, an empty pattern is
 * "no matches yet" not an error). [find] is the exact ported behavior,
 * unchanged; [findWithNamedGroups] is the real extension this plugin
 * adds on top: same match loop, but every match also carries its named
 * groups' extracted values (`year: "2024"`), read via
 * `Matcher.group(name)` -- never [Pattern.namedGroups]'s Map, whose
 * iteration order is not source order (see [NamedGroupExtractor]).
 */
object RegexMatcher {

    fun find(patternText: String, options: RegexOptions, sampleText: String): RegexPreviewResult {
        if (patternText.isEmpty()) return RegexPreviewResult.Success(emptyList())

        val pattern = try {
            Pattern.compile(patternText, options.toJavaFlags())
        } catch (e: PatternSyntaxException) {
            return RegexPreviewResult.Error(e.description ?: e.message ?: "Invalid regex pattern")
        }

        val matcher = pattern.matcher(sampleText)
        val matches = mutableListOf<MatchRange>()
        while (matcher.find()) {
            matches += MatchRange(matcher.start(), matcher.end())
        }
        return RegexPreviewResult.Success(matches)
    }

    fun findWithNamedGroups(patternText: String, options: RegexOptions, sampleText: String): NamedGroupPreviewResult {
        if (patternText.isEmpty()) return NamedGroupPreviewResult.Success(emptyList())

        val groupNames = NamedGroupExtractor.groupNamesInOrder(patternText)

        val pattern = try {
            Pattern.compile(patternText, options.toJavaFlags())
        } catch (e: PatternSyntaxException) {
            return NamedGroupPreviewResult.Error(e.description ?: e.message ?: "Invalid regex pattern")
        }

        val matcher = pattern.matcher(sampleText)
        val matches = mutableListOf<NamedGroupMatch>()
        while (matcher.find()) {
            val range = MatchRange(matcher.start(), matcher.end())
            val groups = groupNames.map { name ->
                // A group inside an untaken alternation branch legitimately
                // has no value for this particular match -- group(name)
                // returns null, never throws, in that case.
                NamedGroupValue(name, matcher.group(name))
            }
            matches += NamedGroupMatch(range, groups)
        }
        return NamedGroupPreviewResult.Success(matches)
    }
}
