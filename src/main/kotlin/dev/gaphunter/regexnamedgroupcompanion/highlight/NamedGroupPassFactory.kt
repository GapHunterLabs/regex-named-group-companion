package dev.gaphunter.regexnamedgroupcompanion.highlight

import com.intellij.codeHighlighting.Pass
import com.intellij.codeHighlighting.TextEditorHighlightingPass
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactory
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactoryRegistrar
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * Registers [NamedGroupHighlightingPass] to run after the IDE's own
 * `Pass.UPDATE_ALL` (general highlighting) pass, same registration
 * shape as Error Lens Companion's `ErrorLensPassFactory` -- this
 * plugin's own scan is independent of that pass's results (it reads
 * PSI, not `HighlightInfo`), but running after general highlighting
 * still avoids competing with it for the daemon's earliest slot.
 */
class NamedGroupPassFactory : TextEditorHighlightingPassFactory, TextEditorHighlightingPassFactoryRegistrar {

    override fun registerHighlightingPassFactory(registrar: TextEditorHighlightingPassRegistrar, project: Project) {
        registrar.registerTextEditorHighlightingPass(
            this,
            null,
            intArrayOf(Pass.UPDATE_ALL),
            false,
            -1,
        )
    }

    override fun createHighlightingPass(file: PsiFile, editor: Editor): TextEditorHighlightingPass? {
        if (editor.isOneLineMode) return null
        if (!file.isPhysical) return null
        if (file.language.id != "JAVA" && file.language.id != "kotlin") return null
        return NamedGroupHighlightingPass(file.project, editor, file)
    }
}
