package com.jokeropal.cards.domain.model

data class JokerOutcome(
    val board: BoardSnapshot,
    val cagesBroken: Int,
    val gemsCleared: Int,
    val armsWildSwap: Boolean
)
