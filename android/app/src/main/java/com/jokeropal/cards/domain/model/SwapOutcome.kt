package com.jokeropal.cards.domain.model

data class SwapOutcome(
    val legal: Boolean,
    val swappedBoard: BoardSnapshot,
    val steps: List<ResolutionStep>
) {

    val gemsCleared: Int
        get() = steps.sumOf { it.gemsCleared }

    val cagesBroken: Int
        get() = steps.sumOf { it.cagesBroken }

    val score: Int
        get() = steps.sumOf { it.score }

    val bestCascade: Int
        get() = steps.size

    val finalBoard: BoardSnapshot
        get() = steps.lastOrNull()?.board ?: swappedBoard
}
