package com.presime.app.ui.stopwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presime.app.ui.components.PulsingDot
import com.presime.app.ui.timer.SessionLabelSheet
import com.presime.app.ui.theme.*

@Composable
fun StopwatchScreen(
    viewModel: StopwatchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showLabelSheet by remember { mutableStateOf(false) }

    if (showLabelSheet) {
        SessionLabelSheet(
            onDismissRequest = { showLabelSheet = false },
            onSave = { label ->
                viewModel.saveAndReset(label)
                showLabelSheet = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STOPWATCH",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkOnBackground
                )
                if (state.isRunning) {
                    Spacer(modifier = Modifier.width(12.dp))
                    PulsingDot()
                }
            }

            // Centered Large Clock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatTime(state.elapsedMs),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 80.sp,
                            color = Color(0xFFFAFAFA)
                        )
                    )
                    Text(
                        text = formatMillis(state.elapsedMs),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 36.sp,
                            color = Amber
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            // Lap list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                itemsIndexed(state.laps) { index, lap ->
                    val isLatest = index == 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isLatest) Amber.copy(alpha = 0.06f)
                                else Color.Transparent
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%02d", lap.id),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isLatest) Amber else DarkMutedText
                        )
                        Text(
                            text = "+${formatTimeLap(lap.splitDeltaMs)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkMutedText
                        )
                        Text(
                            text = formatTimeLap(lap.totalMs),
                            style = MaterialTheme.typography.titleSmall,
                            color = DarkOnBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!state.isRunning && state.elapsedMs == 0L) {
                    PillButton(text = "START", onClick = { viewModel.start() }, isPrimary = true)
                } else if (state.isRunning) {
                    PillButton(text = "LAP", onClick = { viewModel.lap() }, isPrimary = false)
                    PillButton(text = "PAUSE", onClick = { viewModel.pause() }, isPrimary = true)
                } else {
                    // Paused with elapsed time — show SAVE & RESET
                    PillButton(text = "RESET", onClick = { viewModel.reset() }, isDestructive = true)
                    PillButton(text = "SAVE", onClick = { showLabelSheet = true }, isPrimary = true)
                }
            }
        }
    }
}

@Composable
private fun PillButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false
) {
    val bgColor = when {
        isPrimary -> Amber
        isDestructive -> DarkError.copy(alpha = 0.15f)
        else -> DarkSurfaceVariant
    }
    val textColor = when {
        isPrimary -> DarkBackground
        isDestructive -> DarkError
        else -> DarkOnBackground
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .height(52.dp)
            .widthIn(min = 120.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

private fun formatTime(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    val hours = (ms / (1000 * 60 * 60))
    return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}

private fun formatMillis(ms: Long): String {
    return ".${String.format("%02d", (ms % 1000) / 10)}"
}

private fun formatTimeLap(ms: Long): String {
    val millis = (ms % 1000) / 10
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    return String.format("%02d:%02d.%02d", minutes, seconds, millis)
}
