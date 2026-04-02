package com.presime.app.data.repository

import com.presime.app.data.db.FocusSession
import com.presime.app.data.db.dao.DailyFocus
import com.presime.app.data.db.dao.FocusSessionDao
import com.presime.app.data.db.dao.LabelBreakdown
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val focusSessionDao: FocusSessionDao
) {
    suspend fun insertSession(session: FocusSession) {
        focusSessionDao.insertSession(session)
    }

    fun getSessionsFrom(fromTimeMillis: Long): Flow<List<FocusSession>> {
        return focusSessionDao.getSessionsFrom(fromTimeMillis)
    }

    fun getTotalFocusTime(fromTimeMillis: Long): Flow<Long?> {
        return focusSessionDao.getTotalFocusTime(fromTimeMillis)
    }

    fun getDailyFocusTime(fromTimeMillis: Long): Flow<List<DailyFocus>> {
        return focusSessionDao.getDailyFocusTime(fromTimeMillis)
    }

    fun getLabelBreakdown(fromTimeMillis: Long): Flow<List<LabelBreakdown>> {
        return focusSessionDao.getLabelBreakdown(fromTimeMillis)
    }
}
