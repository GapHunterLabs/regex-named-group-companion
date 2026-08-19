package dev.gaphunter.regexnamedgroupcompanion.psi

import com.intellij.psi.PsiFile
import dev.gaphunter.regexnamedgroupcompanion.match.NamedGroupExtractor
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/**
 * Kotlin counterpart of [JavaRegexLiteralFinder]. Same "any string
 * literal in the file", same reason (values are routinely bound to a
 * `val`/constant before use, so scoping to a specific call site would
 * miss the common case).
 *
 * Kotlin has no separate "un-escaped value" accessor at the whole
 * expression level the way `PsiLiteralExpression.getValue()` does for
 * Java, so the raw regex text is rebuilt entry by entry: a
 * [KtLiteralStringTemplateEntry]'s own text is already the literal
 * character sequence (no escape processing needed for those chars),
 * and a [KtEscapeStringTemplateEntry] (`\\`, `\n`, `\t`, ...) is
 * resolved via its real `getUnescapedValue()`. Any *other* entry type
 * (`KtSimpleNameStringTemplateEntry`/`KtBlockStringTemplateEntry`,
 * i.e. real `$name`/`${expr}` interpolation) makes the whole literal
 * un-analyzable as a fixed pattern -- skipped outright rather than
 * guessing, exactly the same "detect nothing rather than something
 * wrong" contract [JavaRegexLiteralFinder] and
 * [dev.gaphunter.regexnamedgroupcompanion.match.NamedGroupExtractor]
 * already follow.
 */
object KotlinRegexLiteralFinder {

    fun findAll(file: PsiFile): List<RegexLiteralHit> {
        val hits = mutableListOf<RegexLiteralHit>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitStringTemplateExpression(expression: KtStringTemplateExpression) {
                super.visitStringTemplateExpression(expression)
                hitFor(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitFor(expression: KtStringTemplateExpression): RegexLiteralHit? {
        val text = reconstructLiteralText(expression) ?: return null
        val groupNames = NamedGroupExtractor.groupNamesInOrder(text)
        if (groupNames.isEmpty()) return null

        val range = expression.textRange
        return RegexLiteralHit(text, range.startOffset, range.endOffset, groupNames)
    }

    /** Returns null (not analyzable as a fixed pattern) the moment any entry is real interpolation, not a literal/escape part. */
    private fun reconstructLiteralText(expression: KtStringTemplateExpression): String? {
        val builder = StringBuilder()
        for (entry in expression.entries) {
            when (entry) {
                is KtLiteralStringTemplateEntry -> builder.append(entry.text)
                is KtEscapeStringTemplateEntry -> builder.append(entry.unescapedValue)
                else -> return null
            }
        }
        return builder.toString()
    }
}
