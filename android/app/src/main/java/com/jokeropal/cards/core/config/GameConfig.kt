package com.jokeropal.cards.core.config

object GameConfig {

    const val LOADER_DURATION_MS = 8000L

    const val BOARD_COLS = 7
    const val BOARD_ROWS = 7
    const val GEM_TYPES = 5

    const val JOKER_CHARGE_FULL = 100
    const val JOKER_CHARGE_PER_GEM = 4

    const val SCORE_PER_GEM = 60
    const val SCORE_PER_CAGE = 250

    const val CHIPS_REWARD_WIN = 180
    const val STARTING_CHIPS = 1250
    const val CAGE_KEY_UNLOCKS = 3

    const val ROUND_END_HOLD_MS = 520L
    const val BOARD_RESHUFFLE_MS = 600L
    const val SWAP_ANIM_MS = 220L
    const val REVERT_ANIM_MS = 180L
    const val CLEAR_ANIM_MS = 200L
    const val FALL_ANIM_MS = 260L
    const val DEAL_STAGGER_MS = 18L
    const val BANNER_HOLD_MS = 1400L

    val MOVE_BUDGET = intArrayOf(28, 26, 24, 24, 22, 22, 20, 20)
    val CAGE_COUNT = intArrayOf(4, 6, 8, 9, 11, 12, 14, 16)

    const val TOTAL_CHAPTERS = 8

    fun moveBudgetFor(chapterIndex: Int): Int {
        val slot = (chapterIndex - 1).coerceIn(0, MOVE_BUDGET.size - 1)
        return MOVE_BUDGET[slot]
    }

    fun cageCountFor(chapterIndex: Int): Int {
        val slot = (chapterIndex - 1).coerceIn(0, CAGE_COUNT.size - 1)
        return CAGE_COUNT[slot]
    }
}
