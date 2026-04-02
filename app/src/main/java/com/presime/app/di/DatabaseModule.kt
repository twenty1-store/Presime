package com.presime.app.di

import android.content.Context
import androidx.room.Room
import com.presime.app.data.db.PresimeDatabase
import com.presime.app.data.db.dao.AppUsageDao
import com.presime.app.data.db.dao.FocusSessionDao
import com.presime.app.data.db.dao.WatchedAppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePresimeDatabase(
        @ApplicationContext context: Context
    ): PresimeDatabase {
        return Room.databaseBuilder(
            context,
            PresimeDatabase::class.java,
            "presime_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideFocusSessionDao(database: PresimeDatabase): FocusSessionDao = database.focusSessionDao()

    @Provides
    fun provideWatchedAppDao(database: PresimeDatabase): WatchedAppDao = database.watchedAppDao()

    @Provides
    fun provideAppUsageDao(database: PresimeDatabase): AppUsageDao = database.appUsageDao()
}
