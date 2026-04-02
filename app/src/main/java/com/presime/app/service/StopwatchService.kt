package com.presime.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
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

data class LapEntry(
    val id: Int,
    val splitDeltaMs: Long,
    val totalMs: Long
)

data class StopwatchState(
    val elapsedMs: Long = 0,
    val isRunning: Boolean = false,
    val laps: List<LapEntry> = emptyList()
)

@AndroidEntryPoint
class StopwatchService : Service() {

    @Inject lateinit var sessionRepository: SessionRepository

    private val binder = LocalBinder()
    
    private val _state = MutableStateFlow(StopwatchState())
    val state: StateFlow<StopwatchState> = _state.asStateFlow()

    private var job: Job? = null
    private var startTimeMillis: Long = 0L
    private var pauseOffsetMillis: Long = 0L
    private var sessionStartEpoch: Long = 0L
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    inner class LocalBinder : Binder() {
        fun getService(): StopwatchService = this@StopwatchService
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
            "RESUME" -> resume()
            "RESET" -> reset()
            "LAP" -> lap()
        }
        return START_STICKY
    }

    fun start() {
        if (_state.value.isRunning) return
        startTimeMillis = System.currentTimeMillis() - pauseOffsetMillis
        if (sessionStartEpoch == 0L) {
            sessionStartEpoch = System.currentTimeMillis()
        }
        _state.update { it.copy(isRunning = true) }
        startForeground(1, createNotification())
        startTimerJob()
    }

    fun pause() {
        if (!_state.value.isRunning) return
        _state.update { it.copy(isRunning = false) }
        pauseOffsetMillis = System.currentTimeMillis() - startTimeMillis
        job?.cancel()
        updateNotification()
    }

    fun resume() {
        start()
    }

    /** Save the stopwatch session to DB, then reset */
    fun saveAndReset(label: String?) {
        val elapsed = _state.value.elapsedMs
        if (elapsed >= 5000) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val finalStartEpoch = sessionStartEpoch
            serviceScope.launch {
                sessionRepository.insertSession(
                    FocusSession(
                        label = label,
                        type = "STOPWATCH",
                        startTime = finalStartEpoch,
                        durationMs = elapsed,
                        date = dateStr
                    )
                )
            }
        }
        resetInternal()
    }

    fun reset() {
        resetInternal()
    }

    private fun resetInternal() {
        pause()
        pauseOffsetMillis = 0L
        sessionStartEpoch = 0L
        _state.update { StopwatchState(elapsedMs = 0, isRunning = false, laps = emptyList()) }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun lap() {
        if (!_state.value.isRunning) return
        val currentElapsed = _state.value.elapsedMs
        val laps = _state.value.laps.toMutableList()
        val previousLapTotal = laps.firstOrNull()?.totalMs ?: 0L
        laps.add(0, LapEntry(
            id = laps.size + 1,
            splitDeltaMs = currentElapsed - previousLapTotal,
            totalMs = currentElapsed
        ))
        _state.update { it.copy(laps = laps) }
    }

    private fun startTimerJob() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTimeMillis
                _state.update { it.copy(elapsedMs = elapsed) }
                if (elapsed % 1000 in 0..50) {
                    updateNotification()
                }
                delay(30)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "stopwatch_channel",
            "Stopwatch",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = (ms / (1000 * 60 * 60))
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun createNotification(): Notification {
        val contentIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val actionIntent = Intent(this, StopwatchService::class.java).apply {
            action = if (_state.value.isRunning) "PAUSE" else "RESUME"
        }
        val actionPendingIntent = PendingIntent.getService(
            this, 1, actionIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val actionTitle = if (_state.value.isRunning) "Pause" else "Resume"

        return NotificationCompat.Builder(this, "stopwatch_channel")
            .setContentTitle("Stopwatch running")
            .setContentText(formatTime(_state.value.elapsedMs))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(0, actionTitle, actionPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        if (!_state.value.isRunning && _state.value.elapsedMs == 0L) return
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, createNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        serviceScope.cancel()
    }
}
