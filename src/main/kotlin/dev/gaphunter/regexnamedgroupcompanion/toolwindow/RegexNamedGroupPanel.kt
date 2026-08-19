package dev.gaphunter.regexnamedgroupcompanion.toolwindow

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import dev.gaphunter.regexnamedgroupcompanion.match.RegexMatcher
import dev.gaphunter.regexnamedgroupcompanion.model.NamedGroupPreviewResult
import dev.gaphunter.regexnamedgroupcompanion.model.RegexOptions
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultHighlighter

/**
 * Regex Preview Companion's tool window (pattern field + sample text +
 * live highlight) extended with a "Named groups" section: every match
 * still gets highlighted exactly as before, but the status area below
 * it now also lists each match's named groups with their real
 * extracted value (`year: "2024"`), not just the total match count.
 * Same "recompute on every keystroke, no debouncing" argument as the
 * original -- `Pattern.compile` + a `Matcher.find()` loop against
 * typically-short sample text stays cheap even with the extra
 * `Matcher.group(name)` calls per match.
 */
class RegexNamedGroupPanel : JPanel(BorderLayout()) {

    private val patternField = JBTextField()
    private val caseInsensitiveCheckbox = JBCheckBox("Case insensitive")
    private val multilineCheckbox = JBCheckBox("Multiline (^\$ match line boundaries)")
    private val dotAllCheckbox = JBCheckBox("Dot matches newline")
    private val sampleTextArea = JBTextArea(10, 60).apply { lineWrap = true; wrapStyleWord = true }
    private val statusLabel = JBLabel(" ")
    private val namedGroupsArea = JBTextArea(6, 60).apply { lineWrap = true; wrapStyleWord = true; isEditable = false }
    private val highlightPainter = DefaultHighlighter.DefaultHighlightPainter(JBColor(Color(255, 235, 59, 120), Color(255, 235, 59, 90)))

    init {
        val topPanel = JPanel(BorderLayout())
        topPanel.add(JBLabel("Pattern: "), BorderLayout.WEST)
        topPanel.add(patternField, BorderLayout.CENTER)

        val flagsPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        flagsPanel.add(caseInsensitiveCheckbox)
        flagsPanel.add(multilineCheckbox)
        flagsPanel.add(dotAllCheckbox)

        val headerPanel = JPanel(BorderLayout())
        headerPanel.add(topPanel, BorderLayout.NORTH)
        headerPanel.add(flagsPanel, BorderLayout.SOUTH)
        headerPanel.border = BorderFactory.createEmptyBorder(8, 8, 4, 8)

        sampleTextArea.border = BorderFactory.createEmptyBorder(4, 4, 4, 4)
        statusLabel.border = BorderFactory.createEmptyBorder(4, 8, 4, 8)
        namedGroupsArea.border = BorderFactory.createEmptyBorder(4, 8, 8, 8)

        val bottomPanel = JPanel(BorderLayout())
        bottomPanel.add(statusLabel, BorderLayout.NORTH)
        bottomPanel.add(JBLabel("Named groups:").apply { border = BorderFactory.createEmptyBorder(0, 8, 0, 8) }, BorderLayout.CENTER)
        bottomPanel.add(JBScrollPane(namedGroupsArea), BorderLayout.SOUTH)

        add(headerPanel, BorderLayout.NORTH)
        add(JBScrollPane(sampleTextArea), BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)

        val listener = object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = updateHighlights()
            override fun removeUpdate(e: DocumentEvent) = updateHighlights()
            override fun changedUpdate(e: DocumentEvent) = updateHighlights()
        }
        patternField.document.addDocumentListener(listener)
        sampleTextArea.document.addDocumentListener(listener)
        caseInsensitiveCheckbox.addActionListener { updateHighlights() }
        multilineCheckbox.addActionListener { updateHighlights() }
        dotAllCheckbox.addActionListener { updateHighlights() }

        updateHighlights()
    }

    /** Exposed for tests -- runs the exact same path the UI listeners trigger, without needing a real keystroke event. */
    fun updateHighlights() {
        sampleTextArea.highlighter.removeAllHighlights()
        val options = RegexOptions(caseInsensitiveCheckbox.isSelected, multilineCheckbox.isSelected, dotAllCheckbox.isSelected)

        when (val result = RegexMatcher.findWithNamedGroups(patternField.text, options, sampleTextArea.text)) {
            is NamedGroupPreviewResult.Success -> {
                for (match in result.matches) {
                    if (match.range.start >= match.range.end) continue // a zero-width match has nothing to visually highlight
                    try {
                        sampleTextArea.highlighter.addHighlight(match.range.start, match.range.end, highlightPainter)
                    } catch (_: BadLocationException) {
                        // Offsets came from matching this exact text moments ago; a
                        // concurrent edit could theoretically race this, in which case
                        // skipping this one highlight is harmless -- the next keystroke
                        // recomputes everything from the current text anyway.
                    }
                }
                statusLabel.text = "${result.matches.size} match(es)"
                statusLabel.foreground = JBColor.foreground()
                namedGroupsArea.text = formatNamedGroups(result)
            }
            is NamedGroupPreviewResult.Error -> {
                statusLabel.text = "Invalid pattern: ${result.message}"
                statusLabel.foreground = JBColor.RED
                namedGroupsArea.text = ""
            }
        }
    }

    private fun formatNamedGroups(result: NamedGroupPreviewResult.Success): String {
        if (result.matches.isEmpty()) return ""
        val hasAnyGroups = result.matches.any { it.groups.isNotEmpty() }
        if (!hasAnyGroups) return "(pattern has no named groups)"

        return result.matches.mapIndexed { index, match ->
            val groupsText = match.groups.joinToString(separator = ", ") { group ->
                val value = group.value?.let { "\"$it\"" } ?: "(did not participate)"
                "${group.name}: $value"
            }
            "Match ${index + 1}: $groupsText"
        }.joinToString(separator = "\n")
    }

    // Exposed for tests only.
    fun setPatternTextForTest(text: String) { patternField.text = text }
    fun setSampleTextForTest(text: String) { sampleTextArea.text = text }
    fun statusTextForTest(): String = statusLabel.text
    fun highlightCountForTest(): Int = sampleTextArea.highlighter.highlights.size
    fun namedGroupsTextForTest(): String = namedGroupsArea.text
}
