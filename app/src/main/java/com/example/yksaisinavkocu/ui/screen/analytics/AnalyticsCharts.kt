package com.example.yksaisinavkocu.ui.screen.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.yksaisinavkocu.data.local.entity.ExamEntity
import com.example.yksaisinavkocu.theme.TextSecondaryDark
import com.example.yksaisinavkocu.theme.TextSecondaryLight
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@Composable
fun NetChangeChart(
    exams: List<ExamEntity>,
    maxNet: Float,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    if (exams.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }
    val axisLabel = rememberAxisLabelComponent(
        color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
    )

    LaunchedEffect(exams) {
        modelProducer.runTransaction {
            lineSeries {
                series(exams.map { it.totalNet.toDouble() })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                rangeProvider = remember(maxNet) {
                    CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = maxNet.toDouble())
                }
            ),
            startAxis = VerticalAxis.rememberStart(label = axisLabel),
            bottomAxis = HorizontalAxis.rememberBottom(label = axisLabel),
        ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}

@Composable
fun SubjectNetChart(
    data: List<Pair<String, Float>>,
    maxNet: Float,
    color: Color,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }
    val axisLabel = rememberAxisLabelComponent(
        color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
    )

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            lineSeries {
                series(data.map { it.second.toDouble() })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                rangeProvider = remember(maxNet) {
                    CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = maxNet.toDouble())
                }
            ),
            startAxis = VerticalAxis.rememberStart(label = axisLabel),
            bottomAxis = HorizontalAxis.rememberBottom(label = axisLabel),
        ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}
