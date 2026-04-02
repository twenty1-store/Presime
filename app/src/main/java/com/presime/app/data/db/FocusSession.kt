package com.presime.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String?,           // nullable — user-assigned label
    val type: String,             // "POMODORO" | "COUNTDOWN" | "STOPWATCH"
    val startTime: Long,          // epoch ms
    val durationMs: Long,
    val date: String              // "yyyy-MM-dd" for easy day grouping
)
