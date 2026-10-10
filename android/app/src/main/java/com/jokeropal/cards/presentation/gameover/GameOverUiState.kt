package com.jokeropal.cards.presentation.gameover

data class GameOverUiState(
    val won: Boolean,
    val chapterIndex: Int,
    val chapterTitle: String,
    val score: Int,
    val cagesBroken: Int,
    val cagesTotal: Int,
    val bestCascade: Int,
    val chipsEarned: Int,
    val totalChips: Int
) {

    val rewardVisible: Boolean
        get() = chipsEarned > 0
}
