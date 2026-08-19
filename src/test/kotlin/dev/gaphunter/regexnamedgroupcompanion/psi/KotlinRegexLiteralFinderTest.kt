package dev.gaphunter.regexnamedgroupcompanion.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KotlinRegexLiteralFinderTest : BasePlatformTestCase() {

    fun testFindsASingleNamedGroupInAKotlinStringLiteral() {
        val file = myFixture.configureByText(
            "Dates.kt",
            """
            val PATTERN = "(?<year>\\d{4})"
            """.trimIndent(),
        )

        val hits = KotlinRegexLiteralFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(listOf("year"), hits[0].groupNames)
    }

    fun testFindsMultipleNamedGroupsInSourceOrder() {
        val file = myFixture.configureByText(
            "Dates.kt",
            """
            val PATTERN = "(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})"
            """.trimIndent(),
        )

        val hits = KotlinRegexLiteralFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(listOf("year", "month", "day"), hits[0].groupNames)
    }

    fun testOrdinaryStringLiteralsWithNoNamedGroupsProduceNoHits() {
        val file = myFixture.configureByText(
            "Greeting.kt",
            """
            val MESSAGE = "hello world"
            val OTHER_REGEX = "\\d{4}-\\d{2}"
            """.trimIndent(),
        )

        val hits = KotlinRegexLiteralFinder.findAll(file)
        assertTrue(hits.isEmpty())
    }

    fun testFindsALiteralUsedDirectlyAtACallSiteNotJustAConstant() {
        val file = myFixture.configureByText(
            "Dates.kt",
            """
            import java.util.regex.Pattern

            fun run() {
                Pattern.compile("(?<code>[A-Z]{2})")
            }
            """.trimIndent(),
        )

        val hits = KotlinRegexLiteralFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(listOf("code"), hits[0].groupNames)
    }

    fun testInterpolatedStringWithARegexLookingNameIsNeverTreatedAsANamedGroup() {
        // A real ${...} interpolation makes the literal un-analyzable as a
        // fixed pattern -- must be skipped, never guessed at.
        val file = myFixture.configureByText(
            "Dates.kt",
            """
            val suffix = "year"
            val PATTERN = "(?<${'$'}suffix>\\d{4})"
            """.trimIndent(),
        )

        val hits = KotlinRegexLiteralFinder.findAll(file)
        assertTrue(hits.isEmpty())
    }

    fun testFindsMultipleSeparateLiteralsInTheSameFile() {
        val file = myFixture.configureByText(
            "Dates.kt",
            """
            val DATE = "(?<year>\\d{4})"
            val CODE = "(?<code>[A-Z]{2})"
            """.trimIndent(),
        )

        val hits = KotlinRegexLiteralFinder.findAll(file)
        assertEquals(2, hits.size)
    }
}
