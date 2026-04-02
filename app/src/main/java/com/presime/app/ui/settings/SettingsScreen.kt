package com.presime.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.presime.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var showAbout by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }

    if (showAbout) {
        AboutSheet(onDismiss = { showAbout = false })
    }
    if (showTerms) {
        TermsSheet(onDismiss = { showTerms = false })
    }
    if (showPrivacy) {
        PrivacySheet(onDismiss = { showPrivacy = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.titleLarge,
            color = DarkOnBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Options
        SettingsItem(title = "About", subtitle = "Learn about Presime") {
            showAbout = true
        }
        SettingsItem(title = "Terms & Conditions", subtitle = "Usage terms") {
            showTerms = true
        }
        SettingsItem(title = "Privacy Policy", subtitle = "Your data stays on your device") {
            showPrivacy = true
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Buy me a coffee
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/presime"))
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Amber)
        ) {
            Text(
                text = "☕  Buy Me a Coffee",
                style = MaterialTheme.typography.titleSmall,
                color = DarkBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "If you find Presime useful, consider supporting\nthe project with a small donation.",
            style = MaterialTheme.typography.bodyLarge,
            color = DarkMutedText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Footer
        Text(
            text = "PRESIME",
            style = MaterialTheme.typography.labelSmall,
            color = DarkMutedText.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Open Source • Made with focus",
            style = MaterialTheme.typography.bodySmall,
            color = DarkMutedText.copy(alpha = 0.3f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = DarkOnBackground)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DarkMutedText)
            }
            Text("›", style = MaterialTheme.typography.headlineMedium, color = DarkMutedText)
        }
    }
}

// ── Bottom Sheets ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = DarkSurface
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("ABOUT", style = MaterialTheme.typography.labelSmall, color = Amber)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Presime",
                style = MaterialTheme.typography.headlineLarge,
                color = DarkOnBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Presime is an open-source productivity app designed to help you focus, track your time, and build better habits.\n\n" +
                "Built with Kotlin, Jetpack Compose, and a love for clean design.\n\n" +
                "• 100% open source\n" +
                "• No accounts required\n" +
                "• No data collection whatsoever\n" +
                "• All data stored locally on your device\n" +
                "• No analytics, no tracking, no ads\n\n" +
                "Your focus sessions, statistics, and preferences never leave your phone. Period.",
                style = MaterialTheme.typography.bodyMedium,
                color = DarkMutedText,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TermsSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("TERMS & CONDITIONS", style = MaterialTheme.typography.labelSmall, color = Amber)
            Spacer(modifier = Modifier.height(16.dp))

            val sections = listOf(
                "1. Acceptance" to
                    "By using Presime, you agree to these terms. Presime is provided as-is, free of charge, as an open-source project.",

                "2. Usage" to
                    "Presime is a personal productivity tool. You may use it freely for personal or professional focus tracking. You may modify, fork, and distribute the source code under the project's open-source license.",

                "3. Data" to
                    "Presime does not collect, transmit, or store any user data on external servers. All focus sessions, timer history, and app settings are stored exclusively on your device using a local database. No information ever leaves your phone.",

                "4. No Warranty" to
                    "Presime is provided without warranty of any kind. The developers are not liable for any data loss, device issues, or other damages arising from the use of this app.",

                "5. Modifications" to
                    "These terms may be updated with new versions of the app. Continued use after updates constitutes acceptance of the revised terms."
            )

            sections.forEach { (title, body) ->
                Text(title, style = MaterialTheme.typography.titleSmall, color = DarkOnBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(body, style = MaterialTheme.typography.bodyMedium, color = DarkMutedText)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacySheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("PRIVACY POLICY", style = MaterialTheme.typography.labelSmall, color = Amber)
            Spacer(modifier = Modifier.height(16.dp))

            val sections = listOf(
                "Data Collection" to
                    "Presime collects zero user data. We do not use analytics, crash reporting services, ad networks, or any third-party SDKs that collect information.",

                "Local Storage Only" to
                    "All your data — focus sessions, timer history, labels, and preferences — is stored in a local Room database on your device. Nothing is synced to any cloud service.",

                "No Network Requests" to
                    "Presime does not make any network requests except when you explicitly tap 'Buy Me a Coffee', which opens your browser. The app functions entirely offline.",

                "No User Accounts" to
                    "There are no accounts, logins, or registration. You are completely anonymous.",

                "Open Source Transparency" to
                    "Presime's source code is publicly available. You can audit every line of code to verify these privacy claims yourself. We believe privacy should be verifiable, not just promised.",

                "Third-Party Services" to
                    "The only external interaction is the optional 'Buy Me a Coffee' link, which opens in your device's browser. Presime has no control over that website's privacy practices.",

                "Your Rights" to
                    "Since all data is stored locally, you have full control. You can clear the app's data or uninstall it at any time to remove everything. No residual data exists on any server."
            )

            sections.forEach { (title, body) ->
                Text(title, style = MaterialTheme.typography.titleSmall, color = DarkOnBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(body, style = MaterialTheme.typography.bodyMedium, color = DarkMutedText)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
