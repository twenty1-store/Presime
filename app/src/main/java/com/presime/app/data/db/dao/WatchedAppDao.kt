package com.presime.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.presime.app.data.db.WatchedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchedAppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchedApp(app: WatchedApp)

    @Query("SELECT * FROM WatchedApp")
    fun getAllWatchedApps(): Flow<List<WatchedApp>>

    @Query("DELETE FROM WatchedApp WHERE packageName = :packageName")
    suspend fun deleteWatchedApp(packageName: String)
}
