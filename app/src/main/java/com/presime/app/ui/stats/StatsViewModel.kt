package com.presime.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presime.app.data.db.FocusSession
import com.presime.app.data.db.dao.DailyFocus
import com.presime.app.data.db.dao.LabelBreakdown
import com.presime.app.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

enum class TimeRange { TODAY, THIS_WEEK, THIS_MONTH }

data class StatsState(
    val timeRange: TimeRange = TimeRange.TODAY,
    val totalFocusTime: Long = 0L,
    val sessions: List<FocusSession> = emptyList(),
    val mostFocusedLabel: String = "",
    val dailyFocus: List<DailyFocus> = emptyList(),
    val labelBreakdown: List<LabelBreakdown> = emptyList()
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _timeRange = MutableStateFlow(TimeRange.TODAY)

    val state: StateFlow<StatsState> = _timeRange.flatMapLatest { range ->
        val fromMillis = getFromMillis(range)

        combine(
            sessionRepository.getSessionsFrom(fromMillis),
            sessionRepository.getDailyFocusTime(fromMillis),
            sessionRepository.getLabelBreakdown(fromMillis)
        ) { sessions, daily, labels ->
            val totalTime = sessions.sumOf { it.durationMs }
            val mostFocused = labels.firstOrNull()?.label ?: "-"

            StatsState(
                timeRange = range,
                totalFocusTime = totalTime,
                sessions = sessions,
                mostFocusedLabel = mostFocused,
                dailyFocus = daily,
                labelBreakdown = labels
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsState()
    )

    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
    }

    private fun getFromMillis(range: TimeRange): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        when (range) {
            TimeRange.TODAY -> {}
            TimeRange.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            }
            TimeRange.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return calendar.timeInMillis
    }
}
