package com.presime.app.ui.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presime.app.service.TimerMode
import com.presime.app.service.TimerService
import com.presime.app.service.TimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private var timerService: TimerService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.LocalBinder
            timerService = binder.getService()
            isBound = true

            viewModelScope.launch {
                timerService?.state?.collect { serviceState ->
                    _state.value = serviceState
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            timerService = null
        }
    }

    init {
        Intent(context, TimerService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun setTimer(minutes: Long, mode: TimerMode = TimerMode.COUNTDOWN) {
        timerService?.setTimer(minutes * 60 * 1000L, mode)
    }

    fun start() {
        timerService?.start()
    }

    fun pause() {
        timerService?.pause()
    }

    fun reset() {
        timerService?.reset()
    }

    fun skip() {
        timerService?.skipPhase()
    }

    fun saveSession(label: String?) {
        timerService?.saveSession(label)
    }

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }
}
