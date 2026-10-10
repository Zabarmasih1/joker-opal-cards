package com.jokeropal.cards.domain.model

data class ChapterResult(
    val chapterIndex: Int,
    val chapterTitle: String,
    val won: Boolean,
    val score: Int,
    val cagesBroken: Int,
    val cagesTotal: Int,
    val bestCascade: Int,
    val chipsEarned: Int
)
