package dev.gaphunter.regexnamedgroupcompanion.highlight

import com.intellij.codeHighlighting.TextEditorHighlightingPass
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import dev.gaphunter.regexnamedgroupcompanion.psi.JavaRegexLiteralFinder
import dev.gaphunter.regexnamedgroupcompanion.psi.KotlinRegexLiteralFinder
import dev.gaphunter.regexnamedgroupcompanion.psi.RegexLiteralHit
import dev.gaphunter.regexnamedgroupcompanion.render.NamedGroupInlayManager

/**
 * Scans [file]'s PSI for regex string literals with named groups and
 * turns each one into an end-of-line inlay showing the group names.
 * Unlike Error Lens Companion's highlighting pass, this one does not
 * read another pass's results -- the "analysis" here is this plugin's
 * own (a single lightweight regex scan per literal, see
 * [dev.gaphunter.regexnamedgroupcompanion.match.NamedGroupExtractor]),
 * cheap enough to redo on every daemon run without debouncing, same
 * argument Regex Preview Companion's tool window already makes for
 * recomputing on every keystroke.
 *
 * [doCollectInformation] runs off the EDT (platform contract for this
 * class) and only reads immutable PSI; [doApplyInformationToEditor]
 * runs on the EDT and is the only place that touches
 * [Editor]/[com.intellij.openapi.editor.InlayModel].
 */
class NamedGroupHighlightingPass(
    project: Project,
    private val editor: Editor,
    private val file: PsiFile,
) : TextEditorHighlightingPass(project, editor.document, false) {

    private var hits: List<RegexLiteralHit> = emptyList()

    override fun doCollectInformation(progress: ProgressIndicator) {
        hits = when (file.language.id) {
            "JAVA" -> JavaRegexLiteralFinder.findAll(file)
            "kotlin" -> KotlinRegexLiteralFinder.findAll(file)
            else -> emptyList()
        }
    }

    override fun doApplyInformationToEditor() {
        val document = editor.document
        val entries = hits.map { hit ->
            val lineNumber = document.getLineNumber(hit.contentStartOffset)
            val lineEndOffset = document.getLineEndOffset(lineNumber)
            lineEndOffset to hit.groupNames.joinToString(prefix = " ", separator = ", ")
        }
        NamedGroupInlayManager.replaceInlays(editor, entries)
    }
}
