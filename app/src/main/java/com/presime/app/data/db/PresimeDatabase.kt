package com.presime.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.presime.app.data.db.dao.AppUsageDao
import com.presime.app.data.db.dao.FocusSessionDao
import com.presime.app.data.db.dao.WatchedAppDao

@Database(
    entities = [FocusSession::class, WatchedApp::class, AppUsageRecord::class],
    version = 1,
    exportSchema = true
)
abstract class PresimeDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun watchedAppDao(): WatchedAppDao
    abstract fun appUsageDao(): AppUsageDao
}
