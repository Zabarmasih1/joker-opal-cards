package com.jokeropal.cards.presentation.game

import com.jokeropal.cards.domain.model.BoardSnapshot
import com.jokeropal.cards.domain.model.ChapterResult
import com.jokeropal.cards.domain.model.JokerState

sealed interface GameUiState {

    enum class Phase {
        DEALING,
        IDLE,
        SELECTED,
        RESOLVING,
        JOKER_FIRING,
        TERMINAL
    }

    object Loading : GameUiState

    data class Playing(
        val phase: Phase,
        val chapterIndex: Int,
        val chapterTitle: String,
        val board: BoardSnapshot,
        val selected: Int,
        val moves: Int,
        val score: Int,
        val cascade: Int,
        val cagesBroken: Int,
        val cagesTotal: Int,
        val jokers: List<JokerState>,
        val bannerText: Int,
        val animate: Boolean,
        val highContrast: Boolean
    ) : GameUiState {

        val interactive: Boolean
            get() = phase == Phase.IDLE || phase == Phase.SELECTED

        val jokerReady: Boolean
            get() = jokers.any { it.isReady }
    }

    data class Finished(val result: ChapterResult) : GameUiState
}
