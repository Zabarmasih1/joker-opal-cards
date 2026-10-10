package com.jokeropal.cards.domain.model

data class PlayerProgress(
    val currentChapter: Int,
    val chaptersCleared: Int,
    val seals: Int,
    val bestCascade: Int,
    val chips: Int
)
