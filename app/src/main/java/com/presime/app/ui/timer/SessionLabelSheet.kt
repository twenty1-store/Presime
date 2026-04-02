package com.presime.app.ui.timer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.presime.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionLabelSheet(
    onDismissRequest: () -> Unit,
    onSave: (String?) -> Unit
) {
    var labelText by remember { mutableStateOf("") }
    val suggestions = listOf("Deep Work", "Reading", "Coding", "Study", "Writing", "Exercise")

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
            Text(
                "SESSION COMPLETE",
                style = MaterialTheme.typography.labelSmall,
                color = Amber
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Label this session (optional)",
                style = MaterialTheme.typography.titleMedium,
                color = DarkOnBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = labelText,
                onValueChange = { labelText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Reading", color = DarkMutedText) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = DarkOnBackground),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Amber,
                    unfocusedBorderColor = DarkBorder,
                    cursorColor = Amber
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(suggestions) { suggestion ->
                    FilterChip(
                        selected = labelText == suggestion,
                        onClick = { labelText = suggestion },
                        label = { Text(suggestion) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Amber.copy(alpha = 0.15f),
                            selectedLabelColor = Amber
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onSave(null) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkMutedText)
                ) {
                    Text("SKIP", style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = { onSave(labelText.takeIf { it.isNotBlank() }) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Amber)
                ) {
                    Text("SAVE", style = MaterialTheme.typography.labelLarge, color = DarkBackground)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
