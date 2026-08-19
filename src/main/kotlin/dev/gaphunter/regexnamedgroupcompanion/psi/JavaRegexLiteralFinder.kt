package dev.gaphunter.regexnamedgroupcompanion.psi

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import dev.gaphunter.regexnamedgroupcompanion.match.NamedGroupExtractor

/**
 * One literal found in source, with the offsets of its *content* (the
 * literal text itself, quotes excluded) inside the file's document --
 * exactly what the inlay hint needs to anchor its end-of-line position
 * to that literal's own line.
 */
data class RegexLiteralHit(
    val contentText: String,
    val contentStartOffset: Int,
    val contentEndOffset: Int,
    val groupNames: List<String>,
)

/**
 * Finds Java string literals that look like a regex with named groups,
 * anywhere in [file] -- deliberately not scoped to a specific call site
 * (`Pattern.compile(...)`, `String.matches(...)`) because a literal is
 * routinely assigned to a constant first
 * (`private static final String DATE = "(?<year>\\d{4})"`) and used
 * elsewhere; scoping to call sites would silently miss that, the most
 * common real pattern in this catalog's own code (see
 * [dev.gaphunter.regexnamedgroupcompanion.match.RegexMatcher]'s own
 * doc comments for a non-hypothetical example). A plain string literal
 * with zero named groups costs one cheap regex scan
 * ([NamedGroupExtractor.groupNamesInOrder]) and produces no hit -- no
 * false positives on ordinary strings.
 */
object JavaRegexLiteralFinder {

    fun findAll(file: PsiFile): List<RegexLiteralHit> {
        val hits = mutableListOf<RegexLiteralHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (element is PsiLiteralExpression) {
                    hitFor(element)?.let { hits += it }
                }
            }
        })
        return hits
    }

    private fun hitFor(literal: PsiLiteralExpression): RegexLiteralHit? {
        val value = literal.value as? String ?: return null
        val groupNames = NamedGroupExtractor.groupNamesInOrder(value)
        if (groupNames.isEmpty()) return null

        // literal.value already un-escapes standard Java string escapes
        // (\\d -> \d etc.), so its offsets don't map 1:1 to the raw
        // source text when escapes are present -- for the inlay's
        // purposes only the literal's overall text range is needed
        // (the hint anchors to end-of-line, not to a specific
        // character inside the literal), so the *element's* range is
        // used, not an offset recomputed from the unescaped value.
        val range = literal.textRange
        return RegexLiteralHit(value, range.startOffset, range.endOffset, groupNames)
    }
}
