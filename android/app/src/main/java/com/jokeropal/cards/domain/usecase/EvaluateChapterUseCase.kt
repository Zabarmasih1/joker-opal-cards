package com.jokeropal.cards.domain.usecase

import com.jokeropal.cards.domain.model.BoardSnapshot
import com.jokeropal.cards.domain.model.ChapterOutcome

class EvaluateChapterUseCase {

    operator fun invoke(board: BoardSnapshot, movesLeft: Int): ChapterOutcome = when {
        board.lockedCount == 0 -> ChapterOutcome.WON
        movesLeft <= 0 -> ChapterOutcome.LOST
        else -> ChapterOutcome.ONGOING
    }
}
