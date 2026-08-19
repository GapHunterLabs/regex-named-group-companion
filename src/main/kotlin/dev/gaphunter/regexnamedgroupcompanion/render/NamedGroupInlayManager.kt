package dev.gaphunter.regexnamedgroupcompanion.render

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key

/**
 * Owns the lifecycle of this plugin's inlays for a given editor, same
 * pattern as Error Lens Companion's `ErrorLensInlayManager`: every pass
 * run calls [replaceInlays] with a fresh set of (offset, text) pairs --
 * the previous set is always disposed first, so re-running the daemon
 * (constantly, while typing) replaces stale hints instead of piling
 * new ones on top.
 */
object NamedGroupInlayManager {

    private val INLAYS_KEY = Key.create<MutableList<Inlay<*>>>("dev.gaphunter.regexnamedgroupcompanion.inlays")

    /** Must be called on the EDT -- true for every real caller (`doApplyInformationToEditor`) and for tests. */
    fun replaceInlays(editor: Editor, entries: List<Pair<Int, String>>) {
        editor.getUserData(INLAYS_KEY)?.forEach { inlay ->
            if (inlay.isValid) Disposer.dispose(inlay)
        }

        val created = entries.mapNotNull { (offset, text) ->
            editor.inlayModel.addAfterLineEndElement(offset, false, NamedGroupInlayRenderer(text))
        }
        editor.putUserData(INLAYS_KEY, created.toMutableList())
    }
}
