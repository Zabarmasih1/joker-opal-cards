package com.jokeropal.cards.data.sample

import com.jokeropal.cards.core.config.GameConfig
import com.jokeropal.cards.domain.model.ChapterDefinition
import com.jokeropal.cards.domain.model.JokerType

object SampleData {

    private val chapterTitles = listOf(
        "THE OPENING CUT",
        "VELVET ANTE",
        "THE GILDED DEAL",
        "MIRROR SUIT",
        "CROWN OF FACETS",
        "THE BLIND REVEAL",
        "WILD CARDINAL",
        "JOKER LAST SEAL"
    )

    val chapters: List<ChapterDefinition> = chapterTitles.mapIndexed { position, title ->
        val index = position + 1
        ChapterDefinition(
            index = index,
            title = title,
            moveBudget = GameConfig.moveBudgetFor(index),
            cageCount = GameConfig.cageCountFor(index)
        )
    }

    val jokers: List<JokerType> = listOf(JokerType.WILD_SWAP, JokerType.ROW_BURN, JokerType.CAGE_KEY)

    const val DEFAULT_CHAPTER = 3
    const val DEFAULT_CHAPTERS_CLEARED = 2
    const val DEFAULT_SEALS = 5
    const val DEFAULT_BEST_CASCADE = 7

    fun titleFor(index: Int): String {
        val slot = (index - 1).coerceIn(0, chapterTitles.size - 1)
        return chapterTitles[slot]
    }
}
