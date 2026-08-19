package dev.gaphunter.regexnamedgroupcompanion.toolwindow

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Deliberately light -- the real matching/extraction logic is
 * exhaustively covered by `RegexMatcherTest` without any Swing
 * involved. This just confirms the panel wires that logic to the UI
 * correctly: a valid pattern with named groups shows both the
 * highlight count and the extracted values, an invalid one shows the
 * real error instead of crashing, and a pattern with no named groups
 * says so instead of showing a blank/confusing "0 groups" state.
 */
class RegexNamedGroupPanelTest : BasePlatformTestCase() {

    fun testValidPatternWithOneNamedGroupShowsItsExtractedValue() {
        val panel = RegexNamedGroupPanel()
        panel.setSampleTextForTest("born in 2024")
        panel.setPatternTextForTest("(?<year>\\d{4})")
        panel.updateHighlights()

        assertEquals(1, panel.highlightCountForTest())
        assertEquals("1 match(es)", panel.statusTextForTest())
        assertTrue(panel.namedGroupsTextForTest().contains("year: \"2024\""))
    }

    fun testMultipleNamedGroupsAllAppearInTheOutput() {
        val panel = RegexNamedGroupPanel()
        panel.setSampleTextForTest("2024-08-19")
        panel.setPatternTextForTest("(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})")
        panel.updateHighlights()

        val text = panel.namedGroupsTextForTest()
        assertTrue(text.contains("year: \"2024\""))
        assertTrue(text.contains("month: \"08\""))
        assertTrue(text.contains("day: \"19\""))
    }

    fun testPatternWithoutNamedGroupsSaysSoInsteadOfShowingNothingConfusing() {
        val panel = RegexNamedGroupPanel()
        panel.setSampleTextForTest("a1 b22 c333")
        panel.setPatternTextForTest("\\d+")
        panel.updateHighlights()

        assertEquals(3, panel.highlightCountForTest())
        assertEquals("(pattern has no named groups)", panel.namedGroupsTextForTest())
    }

    fun testInvalidPatternShowsTheErrorInsteadOfCrashing() {
        val panel = RegexNamedGroupPanel()
        panel.setSampleTextForTest("anything")
        panel.setPatternTextForTest("(?<year>\\d{4}")
        panel.updateHighlights()

        assertEquals(0, panel.highlightCountForTest())
        assertTrue(panel.statusTextForTest().startsWith("Invalid pattern:"))
        assertEquals("", panel.namedGroupsTextForTest())
    }

    fun testEmptyPatternHighlightsNothingWithoutError() {
        val panel = RegexNamedGroupPanel()
        panel.setSampleTextForTest("anything")
        panel.setPatternTextForTest("")
        panel.updateHighlights()

        assertEquals(0, panel.highlightCountForTest())
        assertFalse(panel.statusTextForTest().startsWith("Invalid pattern:"))
    }

    fun testChangingTheSampleTextRecomputesGroupExtractions() {
        val panel = RegexNamedGroupPanel()
        panel.setPatternTextForTest("(?<word>[a-z]+)")
        panel.setSampleTextForTest("cat")
        panel.updateHighlights()
        assertTrue(panel.namedGroupsTextForTest().contains("word: \"cat\""))

        panel.setSampleTextForTest("dog")
        panel.updateHighlights()
        assertTrue(panel.namedGroupsTextForTest().contains("word: \"dog\""))
    }
}
