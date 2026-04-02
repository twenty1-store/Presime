package com.presime.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.presime.app.data.db.AppUsageRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageRecord(record: AppUsageRecord)

    @Query("SELECT * FROM AppUsageRecord WHERE date = :date")
    fun getUsageRecordsForDate(date: String): Flow<List<AppUsageRecord>>

    @Query("SELECT * FROM AppUsageRecord WHERE packageName = :packageName AND date = :date LIMIT 1")
    suspend fun getUsageForAppOnDate(packageName: String, date: String): AppUsageRecord?
}
