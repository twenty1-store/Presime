package com.presime.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.presime.app.MainActivity
import com.presime.app.R
import com.presime.app.data.db.FocusSession
import com.presime.app.data.repository.SessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class TimerMode { COUNTDOWN, POMODORO }
enum class PomodoroPhase { FOCUS, SHORT_BREAK, LONG_BREAK }

data class TimerState(
    val mode: TimerMode = TimerMode.COUNTDOWN,
    val phase: PomodoroPhase = PomodoroPhase.FOCUS,
    val remainingMs: Long = 25 * 60 * 1000L,
    val initialMs: Long = 25 * 60 * 1000L,
    val isRunning: Boolean = false,
    val sessionCount: Int = 1,
    val sessionFinished: Boolean = false
)

@AndroidEntryPoint
class TimerService : Service() {

    @Inject lateinit var sessionRepository: SessionRepository

    private val binder = LocalBinder()
    
    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private var job: Job? = null
    private var endTimeMillis: Long = 0L
    private var pausedRemainingMs: Long = 0L
    private var sessionStartEpoch: Long = 0L
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> start()
            "PAUSE" -> pause()
            "RESET" -> reset()
            "SKIP" -> skipPhase()
        }
        return START_STICKY
    }

    fun setTimer(durationMs: Long, mode: TimerMode) {
        pause()
        _state.update { 
            it.copy(
                mode = mode, 
                remainingMs = durationMs, 
                initialMs = durationMs,
                phase = PomodoroPhase.FOCUS,
                sessionCount = 1,
                sessionFinished = false
            ) 
        }
    }

    fun start() {
        if (_state.value.isRunning) return
        if (_state.value.remainingMs <= 0) return
        
        pausedRemainingMs = _state.value.remainingMs
        endTimeMillis = System.currentTimeMillis() + pausedRemainingMs
        
        // Track when this focus session started
        if (sessionStartEpoch == 0L) {
            sessionStartEpoch = System.currentTimeMillis()
        }
        
        _state.update { it.copy(isRunning = true, sessionFinished = false) }
        startForeground(2, createNotification())
        startTimerJob()
    }

    fun pause() {
        if (!_state.value.isRunning) return
        _state.update { it.copy(isRunning = false) }
        job?.cancel()
        updateNotification()
    }

    fun reset() {
        pause()
        sessionStartEpoch = 0L
        _state.update { it.copy(remainingMs = it.initialMs, sessionFinished = false) }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun skipPhase() {
        pause()
        moveToNextPhase()
    }

    /** Called by ViewModel after the user labels the session or skips */
    fun saveSession(label: String?) {
        val st = _state.value
        val durationMs = st.initialMs - st.remainingMs
        if (durationMs < 5000) return // skip trivial sessions (<5s)

        val type = when {
            st.mode == TimerMode.POMODORO -> "POMODORO"
            else -> "COUNTDOWN"
        }
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val finalStartEpoch = sessionStartEpoch
        sessionStartEpoch = 0L
        _state.update { it.copy(sessionFinished = false) }

        serviceScope.launch {
            sessionRepository.insertSession(
                FocusSession(
                    label = label,
                    type = type,
                    startTime = finalStartEpoch,
                    durationMs = durationMs,
                    date = dateStr
                )
            )
        }
    }

    private fun startTimerJob() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val remaining = endTimeMillis - System.currentTimeMillis()
                if (remaining <= 0) {
                    _state.update { it.copy(remainingMs = 0, isRunning = false, sessionFinished = true) }
                    onTimerFinished()
                    break
                } else {
                    _state.update { it.copy(remainingMs = remaining) }
                    if (remaining % 1000 in 0..50) {
                        updateNotification()
                    }
                }
                delay(30)
            }
        }
    }

    private fun onTimerFinished() {
        playSound()
        if (_state.value.mode == TimerMode.POMODORO) {
            // Auto-save the focus phase, then move to next
            val st = _state.value
            if (st.phase == PomodoroPhase.FOCUS) {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val finalStartEpoch = sessionStartEpoch
                sessionStartEpoch = 0L
                serviceScope.launch {
                    sessionRepository.insertSession(
                        FocusSession(
                            label = null,
                            type = "POMODORO",
                            startTime = finalStartEpoch,
                            durationMs = st.initialMs,
                            date = dateStr
                        )
                    )
                }
            }
            moveToNextPhase()
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(3, NotificationCompat.Builder(this, "timer_channel")
                .setContentTitle("Timer Finished")
                .setContentText("Focus session complete.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .build())
        }
    }

    private fun moveToNextPhase() {
        val st = _state.value
        if (st.mode != TimerMode.POMODORO) return

        val nextPhase: PomodoroPhase
        val nextDurationMin: Long
        var nextSessionCount = st.sessionCount

        when (st.phase) {
            PomodoroPhase.FOCUS -> {
                if (st.sessionCount % 4 == 0) {
                    nextPhase = PomodoroPhase.LONG_BREAK
                    nextDurationMin = 15L
                } else {
                    nextPhase = PomodoroPhase.SHORT_BREAK
                    nextDurationMin = 5L
                }
            }
            PomodoroPhase.SHORT_BREAK, PomodoroPhase.LONG_BREAK -> {
                nextPhase = PomodoroPhase.FOCUS
                nextDurationMin = 25L
                nextSessionCount++
            }
        }

        val ms = nextDurationMin * 60 * 1000L
        _state.update { 
            it.copy(
                phase = nextPhase, 
                remainingMs = ms, 
                initialMs = ms, 
                sessionCount = nextSessionCount,
                isRunning = false,
                sessionFinished = false
            )
        }
        sessionStartEpoch = 0L
        updateNotification()
    }

    private fun playSound() {
        try {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, defaultSoundUri)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "timer_channel",
            "Timer",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60))
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun createNotification(): Notification {
        val contentIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 2, contentIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val actionIntent = Intent(this, TimerService::class.java).apply {
            action = if (_state.value.isRunning) "PAUSE" else "START"
        }
        val actionPendingIntent = PendingIntent.getService(
            this, 3, actionIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val actionTitle = if (_state.value.isRunning) "Pause" else "Resume"
        val title = if (_state.value.mode == TimerMode.POMODORO) {
            when (_state.value.phase) {
                PomodoroPhase.FOCUS -> "Pomodoro Focus"
                PomodoroPhase.SHORT_BREAK -> "Short Break"
                PomodoroPhase.LONG_BREAK -> "Long Break"
            }
        } else {
            "Timer Countdown"
        }

        return NotificationCompat.Builder(this, "timer_channel")
            .setContentTitle(title)
            .setContentText(formatTime(_state.value.remainingMs))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(0, actionTitle, actionPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        if (!_state.value.isRunning && _state.value.remainingMs == _state.value.initialMs) return
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(2, createNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        serviceScope.cancel()
    }
}
