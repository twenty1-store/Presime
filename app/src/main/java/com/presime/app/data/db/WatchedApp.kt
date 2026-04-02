package com.presime.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WatchedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val dailyLimitMs: Long
)
