package com.presime.app.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.presime.app.R
import com.presime.app.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen() {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance()
            delay(1000)
        }
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
    val hourStr = SimpleDateFormat("HH", Locale.getDefault()).format(currentTime.time)
    val hour = currentTime.get(Calendar.HOUR_OF_DAY)

    val greeting = when {
        hour in 5..11 -> "Good morning"
        hour in 12..16 -> "Good afternoon"
        hour in 17..20 -> "Good evening"
        else -> "Good night"
    }

    // Colon blink animation
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colonBlink"
    )

    // Seconds sweep for the ring
    val second = currentTime.get(Calendar.SECOND)
    val sweepAngle by animateFloatAsState(
        targetValue = (second / 60f) * 360f,
        animationSpec = tween(300),
        label = "secondsSweep"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "PRESIME",
                style = MaterialTheme.typography.titleLarge,
                color = DarkOnBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Greeting
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyLarge,
                color = DarkMutedText,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── The clock ring ──
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val strokeWidth = 2.dp.toPx()

                    // Outer circle — thin outline
                    drawCircle(
                        color = DarkBorder,
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Seconds progress arc — amber glow
                    drawArc(
                        color = Amber.copy(alpha = 0.6f),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 3 dot markers at 12, 4, 8 o'clock positions
                    val dotPositions = listOf(
                        -90f,   // 12 o'clock (top)
                        30f,    // ~4 o'clock
                        150f    // ~8 o'clock
                    )
                    val dotRadius = 5.dp.toPx()

                    dotPositions.forEach { angleDeg ->
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val dotCenter = Offset(
                            x = center.x + radius * cos(angleRad).toFloat(),
                            y = center.y + radius * sin(angleRad).toFloat()
                        )
                        drawCircle(
                            color = Amber,
                            radius = dotRadius,
                            center = dotCenter
                        )
                    }
                }

                // Digital time in the center
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val parts = timeFormat.format(currentTime.time).split(":")
                        Text(
                            text = parts.getOrElse(0) { "00" },
                            style = MaterialTheme.typography.displayLarge,
                            color = DarkOnBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.displayLarge,
                            color = DarkOnBackground.copy(alpha = colonAlpha)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = parts.getOrElse(1) { "00" },
                            style = MaterialTheme.typography.displayLarge,
                            color = DarkOnBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = dateFormat.format(currentTime.time).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkMutedText
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Focus summary card — glassmorphism ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = GlassBorder,
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = DarkSurface.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TODAY'S FOCUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Amber
                        )
                        PulsingIndicator()
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "0h 0m",
                        style = MaterialTheme.typography.headlineLarge,
                        color = DarkOnBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Start your first session.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkMutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PulsingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "focusPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "focusPulseAlpha"
    )
    Canvas(modifier = Modifier.size(8.dp)) {
        drawCircle(color = Amber.copy(alpha = alpha))
    }
}
