package dev.gaphunter.regexnamedgroupcompanion.highlight

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Confirms the factory wiring compiles and functions against a real
 * project/editor -- same shape as Error Lens Companion's
 * `ErrorLensPassFactoryTest`, including the same real platform gotcha
 * it already documented: `TextEditorHighlightingPass`'s constructor
 * asserts it is NOT called on the EDT
 * (`ThreadingAssertions.assertBackgroundThread()`), so
 * `createHighlightingPass` must be invoked from a pooled thread here,
 * exactly as the platform's own registrar does in production.
 */
class NamedGroupPassFactoryTest : BasePlatformTestCase() {

    fun testCreateHighlightingPassReturnsARealPassForAJavaFile() {
        myFixture.configureByText("Sample.java", "class Sample {}\n")
        val file = myFixture.file
        val editor = myFixture.editor

        val pass = ApplicationManager.getApplication().executeOnPooledThread<Any?> {
            NamedGroupPassFactory().createHighlightingPass(file, editor)
        }.get()

        assertNotNull(pass)
        assertTrue(pass is NamedGroupHighlightingPass)
    }

    fun testCreateHighlightingPassReturnsARealPassForAKotlinFile() {
        myFixture.configureByText("Sample.kt", "class Sample\n")
        val file = myFixture.file
        val editor = myFixture.editor

        val pass = ApplicationManager.getApplication().executeOnPooledThread<Any?> {
            NamedGroupPassFactory().createHighlightingPass(file, editor)
        }.get()

        assertNotNull(pass)
        assertTrue(pass is NamedGroupHighlightingPass)
    }

    fun testCreateHighlightingPassReturnsNullForAnUnrelatedLanguage() {
        myFixture.configureByText("Sample.txt", "hello world\n")
        val file = myFixture.file
        val editor = myFixture.editor

        val pass = ApplicationManager.getApplication().executeOnPooledThread<Any?> {
            NamedGroupPassFactory().createHighlightingPass(file, editor)
        }.get()

        assertNull(pass)
    }
}
