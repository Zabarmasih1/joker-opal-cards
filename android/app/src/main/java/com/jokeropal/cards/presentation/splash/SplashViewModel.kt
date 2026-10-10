package com.jokeropal.cards.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jokeropal.cards.core.config.GameConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    private val _dots = MutableStateFlow(0)
    val dots: StateFlow<Int> = _dots.asStateFlow()

    private var started = false

    fun start() {
        if (started) return
        started = true
        viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            _finished.value = true
        }
        viewModelScope.launch {
            while (isActive) {
                delay(CAPTION_STEP_MS)
                _dots.value = (_dots.value + 1) % (MAX_DOTS + 1)
            }
        }
    }

    fun consumeFinished() {
        _finished.value = false
    }

    private companion object {
        const val CAPTION_STEP_MS = 420L
        const val MAX_DOTS = 3
    }
}
