package com.jokeropal.cards.domain.model

data class ResolutionStep(
    val cascadeIndex: Int,
    val gemsCleared: Int,
    val cagesBroken: Int,
    val score: Int,
    val board: BoardSnapshot
)
