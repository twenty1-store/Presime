package com.presime.app.ui.timer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presime.app.R
import com.presime.app.service.PomodoroPhase
import com.presime.app.service.TimerMode
import com.presime.app.ui.components.PulsingDot
import com.presime.app.ui.components.RingProgress
import com.presime.app.ui.components.SegmentedPill
import com.presime.app.ui.theme.*

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showPomodoroSettings by remember { mutableStateOf(false) }
    var showLabelSheet by remember { mutableStateOf(false) }

    // Show label sheet when countdown finishes
    LaunchedEffect(state.sessionFinished) {
        if (state.sessionFinished && state.mode == TimerMode.COUNTDOWN) {
            showLabelSheet = true
        }
    }

    if (showTimePicker) {
        CustomTimePicker(
            onDismissRequest = { showTimePicker = false },
            onConfirm = { hours, minutes ->
                val totalMinutes = hours * 60L + minutes
                viewModel.setTimer(totalMinutes, TimerMode.COUNTDOWN)
                showTimePicker = false
            }
        )
    }

    if (showPomodoroSettings) {
        PomodoroSettingsSheet(
            onDismissRequest = { showPomodoroSettings = false },
            onSave = { focus, shortBreak, longBreak ->
                viewModel.setTimer(focus.toLong(), TimerMode.POMODORO)
                showPomodoroSettings = false
            }
        )
    }

    if (showLabelSheet) {
        SessionLabelSheet(
            onDismissRequest = { showLabelSheet = false },
            onSave = { label ->
                viewModel.saveSession(label)
                viewModel.reset()
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TIMER",
                        style = MaterialTheme.typography.titleLarge,
                        color = DarkOnBackground
                    )
                    if (state.isRunning) {
                        Spacer(modifier = Modifier.width(12.dp))
                        PulsingDot()
                    }
                }
                if (selectedTab == 1) {
                    IconButton(onClick = { showPomodoroSettings = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_glyph_settings),
                            contentDescription = "Pomodoro Settings",
                            modifier = Modifier.size(20.dp),
                            tint = DarkMutedText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Segmented pill
            SegmentedPill(
                items = listOf("COUNTDOWN", "POMODORO"),
                selectedIndex = selectedTab,
                onItemSelected = { index ->
                    selectedTab = index
                    if (index == 0) viewModel.setTimer(25, TimerMode.COUNTDOWN)
                    else viewModel.setTimer(25, TimerMode.POMODORO)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pomodoro phase info
            if (selectedTab == 1) {
                val phaseText = when (state.phase) {
                    PomodoroPhase.FOCUS -> "Focus"
                    PomodoroPhase.SHORT_BREAK -> "Short Break"
                    PomodoroPhase.LONG_BREAK -> "Long Break"
                }
                Text(text = phaseText, style = MaterialTheme.typography.headlineMedium, color = DarkOnBackground)

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) { i ->
                        val completed = i < state.sessionCount - 1
                        val isCurrent = i == state.sessionCount - 1
                        Canvas(modifier = Modifier.size(8.dp)) {
                            drawCircle(
                                color = when {
                                    completed -> Amber
                                    isCurrent -> Amber.copy(alpha = 0.5f)
                                    else -> DarkBorder
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Ring progress
            val progress = if (state.initialMs > 0) {
                state.remainingMs.toFloat() / state.initialMs.toFloat()
            } else 0f

            RingProgress(
                progress = progress,
                isActive = state.isRunning,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatTime(state.remainingMs),
                        style = MaterialTheme.typography.displayLarge,
                        color = DarkOnBackground,
                        modifier = Modifier.clickable {
                            if (!state.isRunning) showTimePicker = true
                        }
                    )
                    if (!state.isRunning && state.remainingMs == state.initialMs) {
                        Text(
                            text = "tap to set",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkMutedText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!state.isRunning && state.remainingMs == state.initialMs && !state.sessionFinished) {
                    PillButton(text = "START", onClick = { viewModel.start() }, isPrimary = true)
                } else if (state.isRunning) {
                    if (selectedTab == 1) {
                        PillButton(text = "SKIP", onClick = { viewModel.skip() })
                    } else {
                        PillButton(text = "RESET", onClick = { viewModel.reset() })
                    }
                    PillButton(text = "PAUSE", onClick = { viewModel.pause() }, isPrimary = true)
                } else {
                    PillButton(text = "RESET", onClick = { viewModel.reset() }, isDestructive = true)
                    PillButton(text = "RESUME", onClick = { viewModel.start() }, isPrimary = true)
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
        modifier = Modifier.height(52.dp).widthIn(min = 120.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = textColor)
    }
}

private fun formatTime(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60))
    return String.format("%02d:%02d", minutes, seconds)
}
