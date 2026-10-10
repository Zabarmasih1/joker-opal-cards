package com.jokeropal.cards.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jokeropal.cards.core.config.GameConfig
import com.jokeropal.cards.domain.repository.ChapterRepository
import com.jokeropal.cards.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MenuViewModel(
    private val progressRepository: ProgressRepository,
    private val chapterRepository: ChapterRepository
) : ViewModel() {

    private val _state = MutableStateFlow(emptyState())
    val state: StateFlow<MenuUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val progress = progressRepository.progress()
            val chapter = chapterRepository.chapter(progress.currentChapter)
            _state.value = MenuUiState(
                chapterIndex = chapter.index,
                chapterTitle = chapter.title,
                chaptersCleared = progress.chaptersCleared,
                totalChapters = GameConfig.TOTAL_CHAPTERS,
                seals = progress.seals,
                bestCascade = progress.bestCascade,
                chips = progress.chips
            )
        }
    }

    private fun emptyState(): MenuUiState = MenuUiState(
        chapterIndex = 1,
        chapterTitle = "",
        chaptersCleared = 0,
        totalChapters = GameConfig.TOTAL_CHAPTERS,
        seals = 0,
        bestCascade = 0,
        chips = GameConfig.STARTING_CHIPS
    )
}
