package com.presime.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.presime.app.ui.home.HomeScreen
import com.presime.app.ui.settings.SettingsScreen
import com.presime.app.ui.stats.StatsScreen
import com.presime.app.ui.stopwatch.StopwatchScreen
import com.presime.app.ui.timer.TimerScreen

@Composable
fun PresimeNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.padding(paddingValues)
    ) {
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.Home.route) { HomeScreen() }
        composable(Screen.Stopwatch.route) { StopwatchScreen() }
        composable(Screen.Timer.route) { TimerScreen() }
        composable(Screen.Stats.route) { StatsScreen() }
    }
}
