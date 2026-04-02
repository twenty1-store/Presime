package com.presime.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.presime.app.data.db.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession)

    @Query("SELECT * FROM FocusSession WHERE startTime >= :from ORDER BY startTime DESC")
    fun getSessionsFrom(from: Long): Flow<List<FocusSession>>

    @Query("SELECT SUM(durationMs) FROM FocusSession WHERE startTime >= :from")
    fun getTotalFocusTime(from: Long): Flow<Long?>

    @Query("SELECT date, SUM(durationMs) as totalMs FROM FocusSession WHERE startTime >= :from GROUP BY date ORDER BY date ASC")
    fun getDailyFocusTime(from: Long): Flow<List<DailyFocus>>

    @Query("SELECT label, SUM(durationMs) as totalMs FROM FocusSession WHERE startTime >= :from AND label IS NOT NULL GROUP BY label ORDER BY totalMs DESC")
    fun getLabelBreakdown(from: Long): Flow<List<LabelBreakdown>>
}

data class DailyFocus(val date: String, val totalMs: Long)
data class LabelBreakdown(val label: String, val totalMs: Long)
