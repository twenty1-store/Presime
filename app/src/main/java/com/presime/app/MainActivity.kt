package com.presime.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.presime.app.ui.components.BottomNavBar
import com.presime.app.ui.navigation.PresimeNavGraph
import com.presime.app.ui.theme.PresimeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Force light icons on dark background
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent {
            PresimeTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = com.presime.app.ui.theme.DarkBackground,
                    bottomBar = { BottomNavBar(navController) }
                ) { innerPadding ->
                    PresimeNavGraph(
                        navController = navController,
                        paddingValues = innerPadding
                    )
                }
            }
        }
    }
}