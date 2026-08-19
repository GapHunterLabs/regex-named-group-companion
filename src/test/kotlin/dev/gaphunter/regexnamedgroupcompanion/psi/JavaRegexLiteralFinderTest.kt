package dev.gaphunter.regexnamedgroupcompanion.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JavaRegexLiteralFinderTest : BasePlatformTestCase() {

    fun testFindsASingleNamedGroupInAJavaStringLiteral() {
        val file = myFixture.configureByText(
            "Dates.java",
            """
            class Dates {
                private static final String PATTERN = "(?<year>\\d{4})";
            }
            """.trimIndent(),
        )

        val hits = JavaRegexLiteralFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(listOf("year"), hits[0].groupNames)
    }

    fun testFindsMultipleNamedGroupsInSourceOrder() {
        val file = myFixture.configureByText(
            "Dates.java",
            """
            class Dates {
                private static final String PATTERN = "(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})";
            }
            """.trimIndent(),
        )

        val hits = JavaRegexLiteralFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(listOf("year", "month", "day"), hits[0].groupNames)
    }

    fun testOrdinaryStringLiteralsWithNoNamedGroupsProduceNoHits() {
        val file = myFixture.configureByText(
            "Greeting.java",
            """
            class Greeting {
                private static final String MESSAGE = "hello world";
                private static final String OTHER_REGEX = "\\d{4}-\\d{2}";
            }
            """.trimIndent(),
        )

        val hits = JavaRegexLiteralFinder.findAll(file)
        assertTrue(hits.isEmpty())
    }

    fun testFindsALiteralUsedDirectlyAtACallSiteNotJustAConstant() {
        val file = myFixture.configureByText(
            "Dates.java",
            """
            import java.util.regex.Pattern;

            class Dates {
                void run() {
                    Pattern.compile("(?<code>[A-Z]{2})");
                }
            }
            """.trimIndent(),
        )

        val hits = JavaRegexLiteralFinder.findAll(file)
        assertEquals(1, hits.size)
        assertEquals(listOf("code"), hits[0].groupNames)
    }

    fun testFindsMultipleSeparateLiteralsInTheSameFile() {
        val file = myFixture.configureByText(
            "Dates.java",
            """
            class Dates {
                private static final String DATE = "(?<year>\\d{4})";
                private static final String CODE = "(?<code>[A-Z]{2})";
            }
            """.trimIndent(),
        )

        val hits = JavaRegexLiteralFinder.findAll(file)
        assertEquals(2, hits.size)
    }
}
