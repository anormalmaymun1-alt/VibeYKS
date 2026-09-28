package com.example.yksaisinavkocu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yksaisinavkocu.theme.*

// =========================================================================
// MARKDOWN AST MODEL
// =========================================================================

sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class Bullet(val level: Int, val text: String) : MarkdownBlock()
    data class Numbered(val level: Int, val number: String, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class Quote(val text: String) : MarkdownBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()
    data object HorizontalRule : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data object Space : MarkdownBlock()
}

// =========================================================================
// MAIN COMPOSABLE
// =========================================================================

/**
 * RichTextVisualizer parses and renders rich Markdown text (bold, italic, lists,
 * headers, tables, code blocks) produced by AI models such as Google Gemini.
 */
@Composable
fun RichTextVisualizer(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    isDarkTheme: Boolean = false,
    isUserMessage: Boolean = false,
    allowSelection: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val blocks = remember(text) { parseMarkdownBlocks(text) }
    val effectiveTextColor = if (textColor != Color.Unspecified) {
        textColor
    } else {
        if (isDarkTheme) TextPrimaryDark else MaterialTheme.colorScheme.onSurface
    }

    val content = @Composable {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            blocks.forEach { block ->
                when (block) {
                    is MarkdownBlock.Heading -> {
                        HeadingBlockView(
                            block = block,
                            isDarkTheme = isDarkTheme,
                            isUserMessage = isUserMessage
                        )
                    }

                    is MarkdownBlock.Numbered -> {
                        NumberedBlockView(
                            block = block,
                            textColor = effectiveTextColor,
                            isDarkTheme = isDarkTheme,
                            isUserMessage = isUserMessage,
                            textStyle = textStyle
                        )
                    }

                    is MarkdownBlock.Bullet -> {
                        BulletBlockView(
                            block = block,
                            textColor = effectiveTextColor,
                            isDarkTheme = isDarkTheme,
                            isUserMessage = isUserMessage,
                            textStyle = textStyle
                        )
                    }

                    is MarkdownBlock.Paragraph -> {
                        ParagraphBlockView(
                            block = block,
                            textColor = effectiveTextColor,
                            isDarkTheme = isDarkTheme,
                            isUserMessage = isUserMessage,
                            textStyle = textStyle
                        )
                    }

                    is MarkdownBlock.CodeBlock -> {
                        CodeBlockView(
                            block = block,
                            isDarkTheme = isDarkTheme
                        )
                    }

                    is MarkdownBlock.Table -> {
                        TableBlockView(
                            block = block,
                            isDarkTheme = isDarkTheme
                        )
                    }

                    is MarkdownBlock.Quote -> {
                        QuoteBlockView(
                            block = block,
                            isDarkTheme = isDarkTheme
                        )
                    }

                    is MarkdownBlock.HorizontalRule -> {
                        HorizontalRuleView(isDarkTheme = isDarkTheme)
                    }

                    is MarkdownBlock.Space -> {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }

    if (allowSelection) {
        SelectionContainer {
            content()
        }
    } else {
        content()
    }
}

/**
 * Convenience alias for RichTextVisualizer.
 */
@Composable
fun MarkdownTextVisualizer(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    isDarkTheme: Boolean = false,
    isUserMessage: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) = RichTextVisualizer(
    text = text,
    modifier = modifier,
    textColor = textColor,
    isDarkTheme = isDarkTheme,
    isUserMessage = isUserMessage,
    textStyle = textStyle
)

// =========================================================================
// BLOCK COMPOSABLE VIEWS
// =========================================================================

@Composable
private fun HeadingBlockView(
    block: MarkdownBlock.Heading,
    isDarkTheme: Boolean,
    isUserMessage: Boolean
) {
    val style = when (block.level) {
        1 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        2 -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        else -> MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
    }

    val headingColor = if (isUserMessage) {
        Color.White
    } else when (block.level) {
        1 -> if (isDarkTheme) AeroSkyBlue else AeroDeepBlue
        2 -> if (isDarkTheme) AeroCyan else AeroDeepBlue
        else -> if (isDarkTheme) Color.White else TextPrimaryLight
    }

    Spacer(modifier = Modifier.height(4.dp))
    RichInlineText(
        text = block.text,
        style = style,
        textColor = headingColor,
        isDarkTheme = isDarkTheme,
        isUserMessage = isUserMessage
    )
    Spacer(modifier = Modifier.height(2.dp))
}

@Composable
private fun NumberedBlockView(
    block: MarkdownBlock.Numbered,
    textColor: Color,
    isDarkTheme: Boolean,
    isUserMessage: Boolean,
    textStyle: TextStyle
) {
    val indentDp = if (block.level > 0) (16 * block.level).dp else 0.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indentDp, top = 5.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Modern circular / badge indicator for number
        if (isUserMessage) {
            Text(
                text = "${block.number}.",
                style = textStyle.copy(fontWeight = FontWeight.Bold, color = Color.White),
                modifier = Modifier.padding(end = 6.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp, end = 8.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AeroSkyBlue, AeroDeepBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = block.number,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        RichInlineText(
            text = block.text,
            style = textStyle,
            textColor = textColor,
            isDarkTheme = isDarkTheme,
            isUserMessage = isUserMessage,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BulletBlockView(
    block: MarkdownBlock.Bullet,
    textColor: Color,
    isDarkTheme: Boolean,
    isUserMessage: Boolean,
    textStyle: TextStyle
) {
    val indentDp = if (block.level > 0) (16 * block.level).dp else 0.dp

    val bulletColor = if (isUserMessage) {
        Color.White.copy(alpha = 0.8f)
    } else if (block.level > 0) {
        if (isDarkTheme) AeroCyan else AeroDeepBlue
    } else {
        if (isDarkTheme) AeroSkyBlue else AeroDeepBlue
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indentDp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp, end = 8.dp)
                .size(if (block.level > 0) 5.dp else 6.dp)
                .clip(CircleShape)
                .background(bulletColor)
        )

        RichInlineText(
            text = block.text,
            style = textStyle,
            textColor = textColor,
            isDarkTheme = isDarkTheme,
            isUserMessage = isUserMessage,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ParagraphBlockView(
    block: MarkdownBlock.Paragraph,
    textColor: Color,
    isDarkTheme: Boolean,
    isUserMessage: Boolean,
    textStyle: TextStyle
) {
    RichInlineText(
        text = block.text,
        style = textStyle,
        textColor = textColor,
        isDarkTheme = isDarkTheme,
        isUserMessage = isUserMessage,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CodeBlockView(
    block: MarkdownBlock.CodeBlock,
    isDarkTheme: Boolean
) {
    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFF1E293B)
    val borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.2f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        if (block.language.isNotBlank()) {
            Text(
                text = block.language.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AeroSkyBlue
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Text(
            text = block.code,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color(0xFFF1F5F9),
                lineHeight = 16.sp
            ),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        )
    }
}

@Composable
private fun TableBlockView(
    block: MarkdownBlock.Table,
    isDarkTheme: Boolean
) {
    val borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else AeroMatteBorderLight
    val headerBg = if (isDarkTheme) AeroSkyBlue.copy(alpha = 0.15f) else AeroDeepBlue.copy(alpha = 0.10f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .horizontalScroll(rememberScrollState())
    ) {
        Column {
            // Header Row
            if (block.headers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .background(headerBg)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    block.headers.forEach { header ->
                        Text(
                            text = header,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) AeroSkyBlue else AeroDeepBlue
                            ),
                            modifier = Modifier
                                .widthIn(min = 60.dp, max = 140.dp)
                                .padding(horizontal = 6.dp)
                        )
                    }
                }
                HorizontalDivider(color = borderColor, thickness = 1.dp)
            }

            // Data Rows
            block.rows.forEachIndexed { index, row ->
                val rowBg = if (index % 2 == 1) {
                    if (isDarkTheme) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.03f)
                } else Color.Transparent

                Row(
                    modifier = Modifier
                        .background(rowBg)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    row.forEach { cell ->
                        Text(
                            text = cell,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isDarkTheme) TextPrimaryDark else TextSecondaryLight
                            ),
                            modifier = Modifier
                                .widthIn(min = 60.dp, max = 140.dp)
                                .padding(horizontal = 6.dp)
                        )
                    }
                }
                if (index < block.rows.size - 1) {
                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun QuoteBlockView(
    block: MarkdownBlock.Quote,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
            .background(if (isDarkTheme) AeroSkyBlue.copy(alpha = 0.08f) else AeroSkyBlue.copy(alpha = 0.12f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(24.dp)
                .background(AeroSkyBlue, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        RichInlineText(
            text = block.text,
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            textColor = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight,
            isDarkTheme = isDarkTheme,
            isUserMessage = false
        )
    }
}

@Composable
private fun HorizontalRuleView(isDarkTheme: Boolean) {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        thickness = 1.dp,
        color = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else AeroMatteBorderLight
    )
}

// =========================================================================
// INLINE RICH TEXT RENDERER
// =========================================================================

@Composable
fun RichInlineText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    textColor: Color = Color.Unspecified,
    isDarkTheme: Boolean = false,
    isUserMessage: Boolean = false
) {
    val annotated = remember(text, textColor, isDarkTheme, isUserMessage) {
        buildAnnotatedString {
            appendMarkdownInline(
                text = text,
                isDarkTheme = isDarkTheme,
                defaultColor = textColor,
                isUserMessage = isUserMessage
            )
        }
    }

    Text(
        text = annotated,
        modifier = modifier,
        style = style,
        color = textColor
    )
}

/**
 * Regex for inline markdown tokens:
 * - ***bold italic*** or ___bold italic___
 * - **bold** or __bold__
 * - *italic* or _italic_
 * - `code`
 * - [label](url)
 */
private val INLINE_REGEX = Regex(
    "(\\*\\*\\*(.+?)\\*\\*\\*)|" +
            "(___([^_]+)___)|" +
            "(\\*\\*(.+?)\\*\\*)|" +
            "(__([^_]+)__)|" +
            "(`([^`]+)`)|" +
            "(\\*([^*\\n]+)\\*)|" +
            "((?<=\\s|^|[(\\[{\"'<])_([^_\\n]+)_(?=\\s|$|[)\\]}\"'>.,:;!?]))|" +
            "(\\[([^\\]]+)\\]\\(([^)]+)\\))"
)

private fun AnnotatedString.Builder.appendMarkdownInline(
    text: String,
    isDarkTheme: Boolean,
    defaultColor: Color,
    isUserMessage: Boolean
) {
    var lastIndex = 0
    val matches = INLINE_REGEX.findAll(text)

    for (match in matches) {
        if (match.range.first > lastIndex) {
            append(text.substring(lastIndex, match.range.first))
        }

        val raw = match.value
        when {
            // Bold + Italic (*** or ___)
            (raw.startsWith("***") && raw.endsWith("***") && raw.length >= 6) ||
                    (raw.startsWith("___") && raw.endsWith("___") && raw.length >= 6) -> {
                val inner = raw.substring(3, raw.length - 3)
                val boldColor = if (isUserMessage) Color.White else if (isDarkTheme) Color.White else Color(0xFF0F172A)
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = boldColor
                    )
                ) {
                    append(inner)
                }
            }

            // Bold (** or __)
            (raw.startsWith("**") && raw.endsWith("**") && raw.length >= 4) ||
                    (raw.startsWith("__") && raw.endsWith("__") && raw.length >= 4) -> {
                val inner = raw.substring(2, raw.length - 2)
                val boldColor = if (isUserMessage) Color.White else if (isDarkTheme) Color.White else Color(0xFF0F172A)
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = boldColor
                    )
                ) {
                    append(inner)
                }
            }

            // Inline code (`...`)
            raw.startsWith("`") && raw.endsWith("`") && raw.length >= 2 -> {
                val inner = raw.substring(1, raw.length - 1)
                val codeBg = if (isUserMessage) {
                    Color.White.copy(alpha = 0.25f)
                } else if (isDarkTheme) {
                    Color(0xFF1E293B)
                } else {
                    Color(0xFFE2E8F0)
                }
                val codeColor = if (isUserMessage) Color.White else if (isDarkTheme) AeroSkyBlue else AeroDeepBlue
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        background = codeBg,
                        color = codeColor
                    )
                ) {
                    append(" $inner ")
                }
            }

            // Italic (* or _)
            (raw.startsWith("*") && raw.endsWith("*") && raw.length >= 2) ||
                    (raw.startsWith("_") && raw.endsWith("_") && raw.length >= 2) -> {
                val inner = raw.substring(1, raw.length - 1)
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(inner)
                }
            }

            // Link [text](url)
            raw.startsWith("[") && raw.contains("](") && raw.endsWith(")") -> {
                val label = raw.substring(1, raw.indexOf("]("))
                withStyle(
                    SpanStyle(
                        color = AeroSkyBlue,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(label)
                }
            }

            else -> append(raw)
        }

        lastIndex = match.range.last + 1
    }

    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}

// =========================================================================
// BLOCK PARSER
// =========================================================================

private val BULLET_REGEX = Regex("^(\\s*)([*+\\-•])\\s+(.*)$")
private val NUMBERED_REGEX = Regex("^(\\s*)(\\d+)[.)]\\s+(.*)$")
private val HEADING_REGEX = Regex("^(#{1,6})\\s+(.*)$")
private val QUOTE_REGEX = Regex("^>\\s?(.*)$")
private val HR_REGEX = Regex("^([\\-*_])\\s*(\\1\\s*){2,}$")
private val ACTIONS_COMMENT_REGEX = Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL)

fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val sanitized = rawText.replace(ACTIONS_COMMENT_REGEX, "").trim()
    if (sanitized.isEmpty()) return emptyList()

    val lines = sanitized.replace("\r\n", "\n").replace("\r", "\n").lines()
    val blocks = mutableListOf<MarkdownBlock>()

    var inCodeBlock = false
    var codeBlockLang = ""
    val codeBlockLines = mutableListOf<String>()

    var index = 0
    while (index < lines.size) {
        val line = lines[index]

        // 1. In code block
        if (inCodeBlock) {
            if (line.trimEnd().startsWith("```")) {
                blocks.add(
                    MarkdownBlock.CodeBlock(
                        language = codeBlockLang,
                        code = codeBlockLines.joinToString("\n")
                    )
                )
                inCodeBlock = false
                codeBlockLines.clear()
                codeBlockLang = ""
            } else {
                codeBlockLines.add(line)
            }
            index++
            continue
        }

        // 2. Start code block
        if (line.trimStart().startsWith("```")) {
            inCodeBlock = true
            codeBlockLang = line.trimStart().removePrefix("```").trim()
            codeBlockLines.clear()
            index++
            continue
        }

        // 3. Blank line
        if (line.isBlank()) {
            if (blocks.isNotEmpty() && blocks.last() !is MarkdownBlock.Space) {
                blocks.add(MarkdownBlock.Space)
            }
            index++
            continue
        }

        // 4. Horizontal Rule
        if (HR_REGEX.matches(line.trim())) {
            blocks.add(MarkdownBlock.HorizontalRule)
            index++
            continue
        }

        // 5. Table check: current line starts/ends with | and next line is separator |---|
        if (line.trim().startsWith("|") && line.trim().endsWith("|") && index + 1 < lines.size) {
            val nextLine = lines[index + 1].trim()
            if (nextLine.startsWith("|") && nextLine.contains("---")) {
                val headers = line.split("|")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                val rows = mutableListOf<List<String>>()
                index += 2 // skip header and separator

                while (index < lines.size && lines[index].trim().startsWith("|") && lines[index].trim().endsWith("|")) {
                    val rowCells = lines[index].split("|")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    if (rowCells.isNotEmpty()) {
                        rows.add(rowCells)
                    }
                    index++
                }
                blocks.add(MarkdownBlock.Table(headers, rows))
                continue
            }
        }

        // 6. Heading
        val headingMatch = HEADING_REGEX.find(line.trim())
        if (headingMatch != null) {
            val level = headingMatch.groupValues[1].length
            val headingText = headingMatch.groupValues[2].trim()
            blocks.add(MarkdownBlock.Heading(level, headingText))
            index++
            continue
        }

        // 7. Blockquote
        val quoteMatch = QUOTE_REGEX.find(line.trim())
        if (quoteMatch != null) {
            blocks.add(MarkdownBlock.Quote(quoteMatch.groupValues[1].trim()))
            index++
            continue
        }

        // 8. Numbered item (e.g. "3. **Coğrafya...**")
        val numMatch = NUMBERED_REGEX.find(line)
        if (numMatch != null) {
            val indentStr = numMatch.groupValues[1].replace("\t", "    ")
            val level = if (indentStr.length >= 2) 1 else 0
            val number = numMatch.groupValues[2]
            val itemText = numMatch.groupValues[3].trim()
            blocks.add(MarkdownBlock.Numbered(level, number, itemText))
            index++
            continue
        }

        // 9. Bullet item (e.g. "* **Ölçülebilir Hedef:** ...")
        val bulletMatch = BULLET_REGEX.find(line)
        if (bulletMatch != null) {
            val indentStr = bulletMatch.groupValues[1].replace("\t", "    ")
            val level = if (indentStr.length >= 2) 1 else 0
            val itemText = bulletMatch.groupValues[3].trim()
            blocks.add(MarkdownBlock.Bullet(level, itemText))
            index++
            continue
        }

        // 10. Normal Paragraph
        // If line is continuation of previous bullet or numbered item with leading indent
        val trimmed = line.trim()
        val prevBlock = blocks.lastOrNull()
        if (prevBlock is MarkdownBlock.Bullet && line.startsWith("  ")) {
            val updated = prevBlock.copy(text = "${prevBlock.text} $trimmed")
            blocks[blocks.size - 1] = updated
        } else if (prevBlock is MarkdownBlock.Numbered && line.startsWith("  ")) {
            val updated = prevBlock.copy(text = "${prevBlock.text} $trimmed")
            blocks[blocks.size - 1] = updated
        } else if (prevBlock is MarkdownBlock.Paragraph && !lines.getOrNull(index - 1).isNullOrBlank()) {
            val updated = prevBlock.copy(text = "${prevBlock.text}\n$trimmed")
            blocks[blocks.size - 1] = updated
        } else {
            blocks.add(MarkdownBlock.Paragraph(trimmed))
        }
        index++
    }

    // If still in code block at end of text, close it
    if (inCodeBlock && codeBlockLines.isNotEmpty()) {
        blocks.add(
            MarkdownBlock.CodeBlock(
                language = codeBlockLang,
                code = codeBlockLines.joinToString("\n")
            )
        )
    }

    return blocks
}
