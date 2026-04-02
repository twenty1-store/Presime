package com.presime.app.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presime.app.ui.components.SegmentedPill
import com.presime.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Doughnut chart colors
private val chartColors = listOf(
    Color(0xFFD4A574), // amber
    Color(0xFF8B7355), // warm brown
    Color(0xFF6B8E7B), // muted sage
    Color(0xFFA07855), // copper
    Color(0xFF7B6B8E), // muted purple
    Color(0xFF8E7B6B), // warm taupe
)

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "STATS",
                style = MaterialTheme.typography.titleLarge,
                color = DarkOnBackground
            )
        }

        item {
            SegmentedPill(
                items = listOf("TODAY", "WEEK", "MONTH"),
                selectedIndex = state.timeRange.ordinal,
                onItemSelected = { viewModel.setTimeRange(TimeRange.values()[it]) }
            )
        }

        // Hero cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HeroCard("FOCUS TIME", formatTime(state.totalFocusTime), Modifier.weight(1f))
                HeroCard("SESSIONS", state.sessions.size.toString(), Modifier.weight(1f))
            }
        }

        item {
            HeroCard("TOP FOCUS", state.mostFocusedLabel.ifEmpty { "—" }, Modifier.fillMaxWidth())
        }

        // Area chart — daily focus
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DAILY FOCUS", style = MaterialTheme.typography.labelSmall, color = Amber)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.dailyFocus.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No data yet", style = MaterialTheme.typography.bodyMedium, color = DarkMutedText)
                        }
                    } else {
                        AreaChart(
                            data = state.dailyFocus.map { it.totalMs / (1000f * 60f) },
                            labels = state.dailyFocus.map {
                                it.date.takeLast(2) // day of month
                            },
                            modifier = Modifier.fillMaxWidth().height(160.dp)
                        )
                    }
                }
            }
        }

        // Doughnut chart — label breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("FOCUS BREAKDOWN", style = MaterialTheme.typography.labelSmall, color = Amber)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.labelBreakdown.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No labeled sessions yet", style = MaterialTheme.typography.bodyMedium, color = DarkMutedText)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DoughnutChart(
                                data = state.labelBreakdown.map { it.totalMs.toFloat() },
                                modifier = Modifier.size(140.dp)
                            )
                            Column(
                                modifier = Modifier.weight(1f).padding(start = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.labelBreakdown.take(5).forEachIndexed { index, item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Canvas(modifier = Modifier.size(10.dp)) {
                                            drawCircle(color = chartColors[index % chartColors.size])
                                        }
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DarkOnBackground,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = formatTime(item.totalMs),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DarkMutedText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // History
        item {
            Text("HISTORY", style = MaterialTheme.typography.labelSmall, color = DarkMutedText, modifier = Modifier.padding(top = 8.dp))
        }

        if (state.sessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                        .clip(RoundedCornerShape(20.dp)).background(DarkSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No sessions yet", style = MaterialTheme.typography.bodyLarge, color = DarkMutedText)
                }
            }
        } else {
            items(state.sessions) { session -> SessionCard(session) }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ── Area chart (real data) ──
@Composable
private fun AreaChart(data: List<Float>, labels: List<String>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        val maxVal = data.maxOrNull() ?: 1f
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val points = data.mapIndexed { i, v ->
            Offset(i * stepX, size.height - (v / maxVal * size.height * 0.85f))
        }

        // Fill area
        val fillPath = Path().apply {
            moveTo(0f, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(size.width, size.height)
            close()
        }
        drawPath(fillPath, color = Amber.copy(alpha = 0.12f), style = Fill)

        // Line
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Amber,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Dots
        points.forEach { pt ->
            drawCircle(color = Amber, radius = 4.dp.toPx(), center = pt)
        }
    }
}

// ── Doughnut chart (real data) ──
@Composable
private fun DoughnutChart(data: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val total = data.sum()
        if (total == 0f) return@Canvas
        var startAngle = -90f
        val strokeWidth = 24.dp.toPx()
        val padding = strokeWidth / 2
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

        data.forEachIndexed { index, value ->
            val sweep = (value / total) * 360f
            drawArc(
                color = chartColors[index % chartColors.size],
                startAngle = startAngle,
                sweepAngle = sweep - 2f, // gap between segments
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun HeroCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Amber)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineLarge, color = DarkOnBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SessionCard(session: com.presime.app.data.db.FocusSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(Amber.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(session.label ?: "Unlabeled", style = MaterialTheme.typography.labelSmall, color = Amber)
                    }
                    Text(session.type, style = MaterialTheme.typography.labelSmall, color = DarkMutedText)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(formatDateTime(session.startTime), style = MaterialTheme.typography.bodySmall, color = DarkMutedText)
            }
            Text(formatMinutes(session.durationMs), style = MaterialTheme.typography.titleMedium, color = DarkOnBackground)
        }
    }
}

private fun formatTime(ms: Long): String {
    val minutes = (ms / (1000 * 60)) % 60
    val hours = (ms / (1000 * 60 * 60))
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
private fun formatMinutes(ms: Long): String = "${ms / (1000 * 60)}m"
private fun formatDateTime(epoch: Long): String = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(epoch))
