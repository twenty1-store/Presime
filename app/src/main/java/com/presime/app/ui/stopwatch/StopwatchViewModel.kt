package com.presime.app.ui.stopwatch

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presime.app.service.StopwatchService
import com.presime.app.service.StopwatchState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StopwatchViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(StopwatchState())
    val state: StateFlow<StopwatchState> = _state.asStateFlow()

    private var stopwatchService: StopwatchService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as StopwatchService.LocalBinder
            stopwatchService = binder.getService()
            isBound = true

            viewModelScope.launch {
                stopwatchService?.state?.collect { serviceState ->
                    _state.value = serviceState
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            stopwatchService = null
        }
    }

    init {
        Intent(context, StopwatchService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun start() {
        stopwatchService?.start()
    }

    fun pause() {
        stopwatchService?.pause()
    }

    fun resume() {
        stopwatchService?.resume()
    }

    fun reset() {
        stopwatchService?.reset()
    }

    fun saveAndReset(label: String?) {
        stopwatchService?.saveAndReset(label)
    }

    fun lap() {
        stopwatchService?.lap()
    }

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }
}
