package dev.gaphunter.regexnamedgroupcompanion.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.Alarm
import dev.gaphunter.regexnamedgroupcompanion.match.RegexMatcher
import dev.gaphunter.regexnamedgroupcompanion.model.NamedGroupPreviewResult
import dev.gaphunter.regexnamedgroupcompanion.model.RegexOptions
import dev.gaphunter.regexnamedgroupcompanion.review.ReviewPrompt
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JSplitPane
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
class RegexNamedGroupPanel(private val project: Project? = null) : JPanel(BorderLayout()) {

    private val patternField = JBTextField()
    private val caseInsensitiveCheckbox = JBCheckBox("Case insensitive")
    private val multilineCheckbox = JBCheckBox("Multiline (^\$ match line boundaries)")
    private val dotAllCheckbox = JBCheckBox("Dot matches newline")
    private val sampleTextArea = JBTextArea(10, 60).apply { lineWrap = true; wrapStyleWord = true }
    private val statusLabel = JBLabel(" ")
    private val namedGroupsArea = JBTextArea(6, 60).apply { lineWrap = true; wrapStyleWord = true; isEditable = false }
    private val highlightPainter = DefaultHighlighter.DefaultHighlightPainter(JBColor(Color(255, 235, 59, 120), Color(255, 235, 59, 90)))

    // Live-per-keystroke recompute (see the class doc above) is exactly the
    // "never count a keystroke" case the CTA design warns about -- a
    // separate debounce, only for the CTA signal, turns that continuous
    // stream into one discrete "the user paused after typing something
    // that produced a real match" event, ~800ms after the last edit.
    // No parent Disposable is passed (this panel isn't one).
    private val reviewAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, null)

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

        // Two JBTextArea instances (sampleTextArea, editable, and
        // namedGroupsArea, read-only) used to compete for vertical space
        // inside a single BorderLayout (CENTER + SOUTH). JTextArea's own
        // preferred-size contract is documented as unreliable when its
        // height depends on the width it doesn't know yet at layout time
        // (Oracle Swing docs, java.awt.BorderLayout/JTextArea) -- with
        // two such areas stacked in the same BorderLayout, this produced
        // a real, reproducible visual artifact (stray clipped text from
        // one region bleeding into the other's top-left corner) and a
        // focus bug where sampleTextArea looked focused but rejected all
        // keyboard input. A JSplitPane gives each
        // side an explicit, stable share of the vertical space instead
        // of leaving two ambiguous preferred sizes to be reconciled by
        // BorderLayout -- the same fix pattern used by IntelliJ's own
        // built-in tool windows that stack multiple text areas.
        val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, JBScrollPane(sampleTextArea), bottomPanel)
        splitPane.resizeWeight = 0.6
        splitPane.isContinuousLayout = true

        add(headerPanel, BorderLayout.NORTH)
        add(splitPane, BorderLayout.CENTER)

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
                if (result.matches.isNotEmpty()) scheduleReviewHit()
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

    /**
     * Debounced ~800ms after the last edit -- reset on every call, so a
     * user who keeps typing (still producing matches on every keystroke)
     * never fires this repeatedly; only a real pause after a successful
     * pattern counts as one real "session of use".
     */
    private fun scheduleReviewHit() {
        reviewAlarm.cancelAllRequests()
        reviewAlarm.addRequest({ ReviewPrompt.recordHit(project) }, 800)
    }

    // Exposed for tests only.
    fun setPatternTextForTest(text: String) { patternField.text = text }
    fun setSampleTextForTest(text: String) { sampleTextArea.text = text }
    fun statusTextForTest(): String = statusLabel.text
    fun highlightCountForTest(): Int = sampleTextArea.highlighter.highlights.size
    fun namedGroupsTextForTest(): String = namedGroupsArea.text
}
