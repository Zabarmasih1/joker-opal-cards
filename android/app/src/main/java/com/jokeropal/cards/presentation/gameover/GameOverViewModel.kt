package com.jokeropal.cards.presentation.gameover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jokeropal.cards.domain.model.ChapterResult
import com.jokeropal.cards.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameOverViewModel(
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _state = MutableStateFlow<GameOverUiState?>(null)
    val state: StateFlow<GameOverUiState?> = _state.asStateFlow()

    private var bound = false

    fun bind(result: ChapterResult) {
        if (bound) return
        bound = true
        viewModelScope.launch {
            val progress = progressRepository.progress()
            _state.value = GameOverUiState(
                won = result.won,
                chapterIndex = result.chapterIndex,
                chapterTitle = result.chapterTitle,
                score = result.score,
                cagesBroken = result.cagesBroken,
                cagesTotal = result.cagesTotal,
                bestCascade = result.bestCascade,
                chipsEarned = result.chipsEarned,
                totalChips = progress.chips
            )
        }
    }
}
