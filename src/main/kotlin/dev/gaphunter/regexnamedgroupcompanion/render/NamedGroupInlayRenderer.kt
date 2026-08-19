package dev.gaphunter.regexnamedgroupcompanion.render

import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle

/**
 * Renders one Regex Named Group Companion inlay: the comma-joined
 * group names for one regex literal, pinned to the end of that
 * literal's line. Same deliberately minimal painting as Error Lens
 * Companion's `ErrorLensInlayRenderer` (flat, semi-transparent
 * foreground, editor's own italic font) -- this catalog's proven,
 * low-risk approach to the one part of an inlay that unit tests can
 * only confirm doesn't throw, never that it actually looks right on
 * screen (see README "Known limitations").
 */
class NamedGroupInlayRenderer(private val text: String) : EditorCustomElementRenderer {

    companion object {
        private const val LEFT_PADDING_PX = 12
        private const val ALPHA = 140
    }

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        val editor = inlay.editor
        val font = editor.colorsScheme.getFont(EditorFontType.ITALIC)
        val fontMetrics = editor.contentComponent.getFontMetrics(font)
        return LEFT_PADDING_PX + fontMetrics.stringWidth(text)
    }

    override fun paint(inlay: Inlay<*>, g: Graphics, targetRegion: Rectangle, textAttributes: TextAttributes) {
        val editor = inlay.editor
        val font = editor.colorsScheme.getFont(EditorFontType.ITALIC)
        val foreground = editor.colorsScheme.defaultForeground
        g.font = font
        g.color = Color(foreground.red, foreground.green, foreground.blue, ALPHA)
        val fontMetrics = g.getFontMetrics(font)
        val baseline = targetRegion.y + fontMetrics.ascent
        g.drawString(text, targetRegion.x + LEFT_PADDING_PX, baseline)
    }
}
