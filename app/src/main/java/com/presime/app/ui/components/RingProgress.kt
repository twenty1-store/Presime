package com.presime.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.presime.app.ui.theme.Amber
import com.presime.app.ui.theme.AmberGlow
import com.presime.app.ui.theme.DarkBorder
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RingProgress(
    progress: Float,
    isActive: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400),
        label = "ringProgress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val strokeWidth = if (isActive) 8.dp else 4.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val radius = (size.minDimension - stroke) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            if (isActive) {
                drawCircle(
                    color = Amber.copy(alpha = pulseAlpha),
                    radius = radius * pulseScale,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Background track
            drawCircle(
                color = DarkBorder,
                radius = radius,
                center = center,
                style = Stroke(width = stroke)
            )

            // Progress arc
            val sweepAngle = 360f * animatedProgress
            if (sweepAngle > 0f) {
                drawArc(
                    color = Amber,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )

                // Glow dot at the tip of progress
                val tipAngle = Math.toRadians((-90f + sweepAngle).toDouble())
                val tipCenter = Offset(
                    x = center.x + radius * cos(tipAngle).toFloat(),
                    y = center.y + radius * sin(tipAngle).toFloat()
                )
                drawCircle(
                    color = AmberGlow,
                    radius = stroke * 2.5f,
                    center = tipCenter
                )
                drawCircle(
                    color = Amber,
                    radius = stroke * 0.8f,
                    center = tipCenter
                )
            }
        }
        content()
    }
}
