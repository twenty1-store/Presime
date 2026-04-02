package com.presime.app.ui.timer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.presime.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimePicker(
    onDismissRequest: () -> Unit,
    onConfirm: (hours: Int, minutes: Int) -> Unit
) {
    var hoursText by remember { mutableStateOf("0") }
    var minutesText by remember { mutableStateOf("25") }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "SET TIME",
                style = MaterialTheme.typography.labelSmall,
                color = Amber
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hours
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = {
                            val filtered = it.filter { c -> c.isDigit() }.take(2)
                            hoursText = filtered
                        },
                        modifier = Modifier.width(80.dp),
                        textStyle = MaterialTheme.typography.displaySmall.copy(color = DarkOnBackground),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber,
                            unfocusedBorderColor = DarkBorder,
                            cursorColor = Amber
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("HOURS", style = MaterialTheme.typography.labelSmall, color = DarkMutedText)
                }

                Text(":", style = MaterialTheme.typography.displaySmall, color = DarkMutedText)

                // Minutes
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = {
                            val filtered = it.filter { c -> c.isDigit() }.take(2)
                            minutesText = filtered
                        },
                        modifier = Modifier.width(80.dp),
                        textStyle = MaterialTheme.typography.displaySmall.copy(color = DarkOnBackground),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber,
                            unfocusedBorderColor = DarkBorder,
                            cursorColor = Amber
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("MINS", style = MaterialTheme.typography.labelSmall, color = DarkMutedText)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick presets
            val presets = listOf(5, 10, 15, 25, 30, 45, 60)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets) { mins ->
                    FilterChip(
                        selected = hoursText == "0" && minutesText == mins.toString(),
                        onClick = {
                            hoursText = "0"
                            minutesText = mins.toString()
                        },
                        label = { Text("${mins}m") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Amber.copy(alpha = 0.15f),
                            selectedLabelColor = Amber
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val h = hoursText.toIntOrNull() ?: 0
                    val m = minutesText.toIntOrNull() ?: 0
                    if (h > 0 || m > 0) onConfirm(h, m)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Amber)
            ) {
                Text("SET TIMER", style = MaterialTheme.typography.labelLarge, color = DarkBackground)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
