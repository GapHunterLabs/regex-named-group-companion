package dev.gaphunter.regexnamedgroupcompanion.match

import junit.framework.TestCase

class NamedGroupExtractorTest : TestCase() {

    fun testSingleNamedGroup() {
        val names = NamedGroupExtractor.groupNamesInOrder("(?<year>\\d{4})")
        assertEquals(listOf("year"), names)
    }

    fun testMultipleNamedGroupsPreserveSourceOrder() {
        // Deliberately alphabetically "backwards" so a passing test can't
        // be explained by accidental alphabetical sorting somewhere.
        val names = NamedGroupExtractor.groupNamesInOrder("(?<zebra>\\d+)-(?<apple>\\d+)-(?<mango>\\d+)")
        assertEquals(listOf("zebra", "apple", "mango"), names)
    }

    fun testPatternWithNoNamedGroupsReturnsEmptyList() {
        val names = NamedGroupExtractor.groupNamesInOrder("\\d{4}-\\d{2}-\\d{2}")
        assertEquals(emptyList<String>(), names)
    }

    fun testUnnamedCapturingGroupsAreNotCountedAsNamed() {
        val names = NamedGroupExtractor.groupNamesInOrder("(\\d{4})-(\\d{2})")
        assertEquals(emptyList<String>(), names)
    }

    fun testEmptyPatternReturnsEmptyList() {
        assertEquals(emptyList<String>(), NamedGroupExtractor.groupNamesInOrder(""))
    }

    fun testLookbehindIsNeverMistakenForANamedGroup() {
        // (?<= and (?<! share the "(?<" prefix with named groups but are
        // lookbehind assertions, not named groups -- a naive regex over
        // the pattern text could easily conflate the two.
        val positiveLookbehind = NamedGroupExtractor.groupNamesInOrder("(?<=USD)\\d+")
        assertEquals(emptyList<String>(), positiveLookbehind)

        val negativeLookbehind = NamedGroupExtractor.groupNamesInOrder("(?<!USD)\\d+")
        assertEquals(emptyList<String>(), negativeLookbehind)
    }

    fun testLookbehindMixedWithARealNamedGroupOnlyCountsTheNamedGroup() {
        val names = NamedGroupExtractor.groupNamesInOrder("(?<=USD)(?<amount>\\d+)")
        assertEquals(listOf("amount"), names)
    }

    fun testDuplicateGroupNameInAnAlternationIsListedOnce() {
        // Java allows the same named group in mutually-exclusive alternation
        // branches, e.g. (?<code>US)|(?<code>CA) is actually a
        // PatternSyntaxException in java.util.regex (duplicate names are NOT
        // allowed even in alternation, unlike some other regex engines) --
        // but the pure text scan doesn't compile, so it still returns the
        // name once, deduplicated; hasNamedGroups() is what rejects the
        // pattern as invalid.
        val names = NamedGroupExtractor.groupNamesInOrder("(?<code>US)|(?<code>CA)")
        assertEquals(listOf("code"), names)
    }

    fun testInvalidPatternWithADuplicateNameIsRejectedByHasNamedGroups() {
        assertFalse(NamedGroupExtractor.hasNamedGroups("(?<code>US)|(?<code>CA)"))
    }

    fun testHasNamedGroupsIsTrueForAValidPatternWithAGroup() {
        assertTrue(NamedGroupExtractor.hasNamedGroups("(?<year>\\d{4})"))
    }

    fun testHasNamedGroupsIsFalseWhenNoNamedGroupsPresent() {
        assertFalse(NamedGroupExtractor.hasNamedGroups("\\d{4}"))
    }

    fun testHasNamedGroupsIsFalseForAnUnparsablePatternEvenIfItLooksLikeItHasAName() {
        // Unclosed group -- never compiles, so never counted as "has named groups"
        // even though the text technically contains "(?<year>".
        assertFalse(NamedGroupExtractor.hasNamedGroups("(?<year>\\d{4}"))
    }
}
