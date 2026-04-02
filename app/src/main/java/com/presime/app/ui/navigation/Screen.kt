package com.presime.app.ui.navigation

import androidx.annotation.DrawableRes
import com.presime.app.R

sealed class Screen(val route: String, val title: String, @DrawableRes val iconRes: Int) {
    object Home : Screen("home", "Home", R.drawable.ic_glyph_home)
    object Stopwatch : Screen("stopwatch", "Stopwatch", R.drawable.ic_glyph_stopwatch)
    object Timer : Screen("timer", "Timer", R.drawable.ic_glyph_timer)
    object Stats : Screen("stats", "Stats", R.drawable.ic_glyph_stats)
    object Settings : Screen("settings", "Settings", R.drawable.ic_glyph_settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Stopwatch,
    Screen.Timer,
    Screen.Stats,
    Screen.Settings
)
