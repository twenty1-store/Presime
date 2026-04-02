package com.presime.app.ui.timer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.presime.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroSettingsSheet(
    currentFocus: Int = 25,
    currentShortBreak: Int = 5,
    currentLongBreak: Int = 15,
    onDismissRequest: () -> Unit,
    onSave: (focus: Int, shortBreak: Int, longBreak: Int) -> Unit
) {
    var focus by remember { mutableStateOf(currentFocus.toFloat()) }
    var shortBreak by remember { mutableStateOf(currentShortBreak.toFloat()) }
    var longBreak by remember { mutableStateOf(currentLongBreak.toFloat()) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("POMODORO SETTINGS", style = MaterialTheme.typography.labelSmall, color = Amber)

            Spacer(modifier = Modifier.height(24.dp))

            // Focus duration
            DurationSlider(
                label = "Focus",
                value = focus,
                range = 5f..60f,
                onValueChange = { focus = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Short break
            DurationSlider(
                label = "Short Break",
                value = shortBreak,
                range = 1f..15f,
                onValueChange = { shortBreak = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Long break
            DurationSlider(
                label = "Long Break",
                value = longBreak,
                range = 5f..30f,
                onValueChange = { longBreak = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onSave(focus.toInt(), shortBreak.toInt(), longBreak.toInt()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Amber)
            ) {
                Text("SAVE", style = MaterialTheme.typography.labelLarge, color = DarkBackground)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DurationSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = DarkOnBackground)
            Text(
                "${value.toInt()} min",
                style = MaterialTheme.typography.titleMedium,
                color = Amber
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = ((range.endInclusive - range.start) / 5).toInt() - 1,
            colors = SliderDefaults.colors(
                thumbColor = Amber,
                activeTrackColor = Amber,
                inactiveTrackColor = DarkSurfaceVariant
            )
        )
    }
}
