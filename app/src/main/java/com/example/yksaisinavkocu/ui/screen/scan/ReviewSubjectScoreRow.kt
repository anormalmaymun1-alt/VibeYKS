package com.example.yksaisinavkocu.ui.screen.scan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.yksaisinavkocu.data.local.entity.SubjectResult
import com.example.yksaisinavkocu.theme.*
import com.example.yksaisinavkocu.ui.components.GlassCard

@Composable
fun SubjectEditCard(
    subject: SubjectResult,
    onUpdate: (SubjectResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val subjColor = SubjectColors[subject.subjectName] ?: AeroSkyBlue

    GlassCard(
        cornerRadius = 16.dp,
        alpha = 0.08f,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subject.subjectName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = subjColor
                )
                Text(
                    text = "%.2f net".format(subject.net),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = subjColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Doğru
                NumberField(
                    label = "Doğru",
                    value = subject.correct,
                    color = AeroLeafGreen,
                    modifier = Modifier.weight(1f),
                    onValueChange = { c ->
                        val calcNet = c - (subject.wrong / 4.0f)
                        onUpdate(subject.copy(correct = c, net = calcNet))
                    }
                )

                // Yanlış
                NumberField(
                    label = "Yanlış",
                    value = subject.wrong,
                    color = ErrorRed,
                    modifier = Modifier.weight(1f),
                    onValueChange = { w ->
                        val calcNet = subject.correct - (w / 4.0f)
                        onUpdate(subject.copy(wrong = w, net = calcNet))
                    }
                )

                // Boş
                NumberField(
                    label = "Boş",
                    value = subject.empty,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                    onValueChange = { b ->
                        onUpdate(subject.copy(empty = b))
                    }
                )
            }
        }
    }
}

@Composable
fun NumberField(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit
) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { str ->
            val num = str.filter { it.isDigit() }.toIntOrNull() ?: 0
            onValueChange(num)
        },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = color,
            unfocusedBorderColor = color.copy(alpha = 0.4f),
            focusedLabelColor = color
        )
    )
}

fun getDefaultSubjectsFor(examType: String): List<SubjectResult> {
    return if (examType == "AYT") {
        listOf(
            SubjectResult("Matematik", 0, 0, 0, 0f),
            SubjectResult("Fizik", 0, 0, 0, 0f),
            SubjectResult("Kimya", 0, 0, 0, 0f),
            SubjectResult("Biyoloji", 0, 0, 0, 0f)
        )
    } else {
        listOf(
            SubjectResult("Türkçe", 0, 0, 0, 0f),
            SubjectResult("Sosyal Bilimler", 0, 0, 0, 0f),
            SubjectResult("Temel Matematik", 0, 0, 0, 0f),
            SubjectResult("Fen Bilimleri", 0, 0, 0, 0f)
        )
    }
}
