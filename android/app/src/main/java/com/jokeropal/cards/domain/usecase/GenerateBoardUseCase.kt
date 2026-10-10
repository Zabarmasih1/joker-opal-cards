package com.jokeropal.cards.domain.usecase

import com.jokeropal.cards.core.config.GameConfig
import com.jokeropal.cards.domain.model.BoardCell
import com.jokeropal.cards.domain.model.BoardSnapshot
import com.jokeropal.cards.domain.model.ChapterDefinition
import com.jokeropal.cards.domain.model.OpalGem
import kotlin.random.Random

class GenerateBoardUseCase(private val random: Random = Random.Default) {

    operator fun invoke(chapter: ChapterDefinition): BoardSnapshot {
        val rows = GameConfig.BOARD_ROWS
        val cols = GameConfig.BOARD_COLS
        val palette = OpalGem.values().take(GameConfig.GEM_TYPES)
        val locked = pickCages(rows, cols, chapter.cageCount)
        val cells = ArrayList<BoardCell>(rows * cols)
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val banned = HashSet<OpalGem>()
                if (col >= 2) {
                    val left = cells[row * cols + col - 1].gem
                    val farLeft = cells[row * cols + col - 2].gem
                    if (left == farLeft) banned.add(left)
                }
                if (row >= 2) {
                    val up = cells[(row - 1) * cols + col].gem
                    val farUp = cells[(row - 2) * cols + col].gem
                    if (up == farUp) banned.add(up)
                }
                val choices = palette.filterNot { banned.contains(it) }
                val gem = choices[random.nextInt(choices.size)]
                cells.add(
                    BoardCell(
                        row = row,
                        col = col,
                        gem = gem,
                        locked = locked.contains(row * cols + col),
                        wild = false
                    )
                )
            }
        }
        return BoardSnapshot(rows = rows, cols = cols, cells = cells)
    }

    fun reshuffle(board: BoardSnapshot): BoardSnapshot {
        val palette = OpalGem.values().take(GameConfig.GEM_TYPES)
        val cells = board.cells.map { cell ->
            if (cell.wild) cell else cell.copy(gem = palette[random.nextInt(palette.size)])
        }
        return board.withCells(cells)
    }

    private fun pickCages(rows: Int, cols: Int, count: Int): Set<Int> {
        val total = rows * cols
        val wanted = count.coerceIn(0, total / 3)
        val picked = LinkedHashSet<Int>()
        var guard = 0
        while (picked.size < wanted && guard < total * 8) {
            guard++
            val row = 1 + random.nextInt(rows - 2)
            val col = 1 + random.nextInt(cols - 2)
            picked.add(row * cols + col)
        }
        return picked
    }
}
