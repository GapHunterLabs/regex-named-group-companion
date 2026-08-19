package dev.gaphunter.regexnamedgroupcompanion.render

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Exercises the real `InlayModel.addAfterLineEndElement` / `Disposer`
 * calls -- same de-risking Error Lens Companion's
 * `ErrorLensInlayManagerTest` already established for this exact
 * pattern. Does NOT verify actual on-screen pixels (see README.md
 * "Known limitations").
 */
class NamedGroupInlayManagerTest : BasePlatformTestCase() {

    fun testReplaceInlaysAddsOneInlayPerEntry() {
        myFixture.configureByText("Sample.txt", "line one\nline two\nline three\n")
        val editor = myFixture.editor
        val document = editor.document

        NamedGroupInlayManager.replaceInlays(
            editor,
            listOf(
                document.getLineEndOffset(0) to " year, month",
                document.getLineEndOffset(1) to " code",
            ),
        )

        val inlays = editor.inlayModel.getAfterLineEndElementsInRange(0, document.textLength)
        assertEquals(2, inlays.size)
    }

    fun testReplaceInlaysDisposesPreviousInlaysInsteadOfAccumulating() {
        myFixture.configureByText("Sample.txt", "line one\nline two\n")
        val editor = myFixture.editor
        val document = editor.document

        NamedGroupInlayManager.replaceInlays(editor, listOf(document.getLineEndOffset(0) to " year"))
        NamedGroupInlayManager.replaceInlays(editor, listOf(document.getLineEndOffset(0) to " year, month"))

        val inlays = editor.inlayModel.getAfterLineEndElementsInRange(0, document.textLength)
        assertEquals(1, inlays.size)
    }

    fun testReplaceInlaysWithEmptyListClearsExistingOnes() {
        myFixture.configureByText("Sample.txt", "line one\n")
        val editor = myFixture.editor
        val document = editor.document

        NamedGroupInlayManager.replaceInlays(editor, listOf(document.getLineEndOffset(0) to " year"))
        NamedGroupInlayManager.replaceInlays(editor, emptyList())

        val inlays = editor.inlayModel.getAfterLineEndElementsInRange(0, document.textLength)
        assertEquals(0, inlays.size)
    }
}
