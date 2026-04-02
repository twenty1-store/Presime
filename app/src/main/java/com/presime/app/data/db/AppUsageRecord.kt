package com.presime.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppUsageRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val date: String,
    val usedMs: Long
)
