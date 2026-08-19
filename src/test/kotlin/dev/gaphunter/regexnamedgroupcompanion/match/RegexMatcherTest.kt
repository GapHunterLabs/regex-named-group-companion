package dev.gaphunter.regexnamedgroupcompanion.match

import dev.gaphunter.regexnamedgroupcompanion.model.MatchRange
import dev.gaphunter.regexnamedgroupcompanion.model.NamedGroupPreviewResult
import dev.gaphunter.regexnamedgroupcompanion.model.RegexOptions
import dev.gaphunter.regexnamedgroupcompanion.model.RegexPreviewResult
import junit.framework.TestCase

class RegexMatcherTest : TestCase() {

    // -- find() -- ported unchanged from Regex Preview Companion, same coverage.

    fun testFindsASimpleLiteralMatch() {
        val result = RegexMatcher.find("cat", RegexOptions(), "the cat sat")
        assertEquals(RegexPreviewResult.Success(listOf(MatchRange(4, 7))), result)
    }

    fun testEmptyPatternMatchesNothingAndIsNotAnError() {
        val result = RegexMatcher.find("", RegexOptions(), "anything")
        assertEquals(RegexPreviewResult.Success(emptyList()), result)
    }

    fun testInvalidPatternReturnsTheRealSyntaxExceptionMessage() {
        val result = RegexMatcher.find("(unclosed", RegexOptions(), "text") as RegexPreviewResult.Error
        assertTrue(result.message.isNotBlank())
    }

    fun testCaseInsensitiveFlagIsRespected() {
        val insensitive = RegexMatcher.find("CAT", RegexOptions(caseInsensitive = true), "the cat sat") as RegexPreviewResult.Success
        assertEquals(1, insensitive.matches.size)
    }

    // -- findWithNamedGroups() -- the real extension this plugin adds.

    fun testSingleNamedGroupExtractsItsRealValue() {
        val result = RegexMatcher.findWithNamedGroups("(?<year>\\d{4})", RegexOptions(), "born in 2024") as NamedGroupPreviewResult.Success
        assertEquals(1, result.matches.size)
        assertEquals(listOf(dev.gaphunter.regexnamedgroupcompanion.model.NamedGroupValue("year", "2024")), result.matches[0].groups)
    }

    fun testMultipleNamedGroupsEachExtractTheirOwnValueInSourceOrder() {
        val result = RegexMatcher.findWithNamedGroups(
            "(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})",
            RegexOptions(),
            "2024-08-19",
        ) as NamedGroupPreviewResult.Success

        assertEquals(1, result.matches.size)
        val groups = result.matches[0].groups
        assertEquals(3, groups.size)
        assertEquals("year", groups[0].name)
        assertEquals("2024", groups[0].value)
        assertEquals("month", groups[1].name)
        assertEquals("08", groups[1].value)
        assertEquals("day", groups[2].name)
        assertEquals("19", groups[2].value)
    }

    fun testPatternWithNoNamedGroupsStillMatchesWithAnEmptyGroupsList() {
        val result = RegexMatcher.findWithNamedGroups("\\d{4}", RegexOptions(), "year 2024") as NamedGroupPreviewResult.Success
        assertEquals(1, result.matches.size)
        assertEquals(emptyList<Any>(), result.matches[0].groups)
    }

    fun testInvalidPatternReturnsTheRealErrorNeverCrashing() {
        val result = RegexMatcher.findWithNamedGroups("(?<year>\\d{4}", RegexOptions(), "text") as NamedGroupPreviewResult.Error
        assertTrue(result.message.isNotBlank())
    }

    fun testEmptyPatternIsNotAnErrorForNamedGroupsEither() {
        val result = RegexMatcher.findWithNamedGroups("", RegexOptions(), "anything")
        assertEquals(NamedGroupPreviewResult.Success(emptyList()), result)
    }

    fun testMultipleMatchesEachGetTheirOwnGroupExtraction() {
        val result = RegexMatcher.findWithNamedGroups(
            "(?<num>\\d+)",
            RegexOptions(),
            "a1 b22 c333",
        ) as NamedGroupPreviewResult.Success

        assertEquals(3, result.matches.size)
        assertEquals("1", result.matches[0].groups.single().value)
        assertEquals("22", result.matches[1].groups.single().value)
        assertEquals("333", result.matches[2].groups.single().value)
    }

    fun testGroupInsideAnUntakenAlternationBranchHasANullValueNotACrash() {
        val result = RegexMatcher.findWithNamedGroups(
            "(?<letters>[a-z]+)|(?<digits>[0-9]+)",
            RegexOptions(),
            "abc",
        ) as NamedGroupPreviewResult.Success

        assertEquals(1, result.matches.size)
        val groups = result.matches[0].groups.associate { it.name to it.value }
        assertEquals("abc", groups["letters"])
        assertNull(groups["digits"])
    }

    fun testCaseInsensitiveFlagAppliesToNamedGroupMatchingToo() {
        val result = RegexMatcher.findWithNamedGroups(
            "(?<word>CAT)",
            RegexOptions(caseInsensitive = true),
            "the cat sat",
        ) as NamedGroupPreviewResult.Success

        assertEquals(1, result.matches.size)
        assertEquals("cat", result.matches[0].groups.single().value)
    }
}
