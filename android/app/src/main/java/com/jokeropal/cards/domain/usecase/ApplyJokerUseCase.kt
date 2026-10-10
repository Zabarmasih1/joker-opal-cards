package com.jokeropal.cards.domain.usecase

import com.jokeropal.cards.core.config.GameConfig
import com.jokeropal.cards.domain.model.BoardSnapshot
import com.jokeropal.cards.domain.model.JokerOutcome
import com.jokeropal.cards.domain.model.JokerType
import kotlin.math.abs

class ApplyJokerUseCase(private val resolveSwapUseCase: ResolveSwapUseCase) {

    operator fun invoke(board: BoardSnapshot, type: JokerType): JokerOutcome = when (type) {
        JokerType.WILD_SWAP -> armWildSwap(board)
        JokerType.ROW_BURN -> burnRow(board)
        JokerType.CAGE_KEY -> openCages(board)
    }

    private fun armWildSwap(board: BoardSnapshot): JokerOutcome {
        val middle = board.indexOf(board.rows / 2, board.cols / 2)
        val cells = board.cells.toMutableList()
        val target = cells[middle]
        if (!target.locked) {
            cells[middle] = target.copy(wild = true)
        }
        return JokerOutcome(
            board = board.withCells(cells),
            cagesBroken = 0,
            gemsCleared = 0,
            armsWildSwap = true
        )
    }

    private fun burnRow(board: BoardSnapshot): JokerOutcome {
        var bestRow = 0
        var bestScore = -1
        for (row in 0 until board.rows) {
            var score = 0
            for (col in 0 until board.cols) {
                if (board.cellAt(row, col).locked) score += 3
            }
            if (score > bestScore) {
                bestScore = score
                bestRow = row
            }
        }
        val cages = LinkedHashSet<Int>()
        val cleared = LinkedHashSet<Int>()
        for (col in 0 until board.cols) {
            val index = board.indexOf(bestRow, col)
            if (board.cellAt(index).locked) cages.add(index) else cleared.add(index)
        }
        val opened = board.withCells(
            board.cells.map { cell ->
                val index = board.indexOf(cell.row, cell.col)
                if (cages.contains(index)) cell.copy(locked = false) else cell
            }
        )
        val collapsed = resolveSwapUseCase.collapse(opened, cleared)
        return JokerOutcome(
            board = collapsed,
            cagesBroken = cages.size,
            gemsCleared = cleared.size,
            armsWildSwap = false
        )
    }

    private fun openCages(board: BoardSnapshot): JokerOutcome {
        val centerRow = board.rows / 2
        val centerCol = board.cols / 2
        val targets = board.cells
            .filter { it.locked }
            .sortedBy { abs(it.row - centerRow) + abs(it.col - centerCol) }
            .take(GameConfig.CAGE_KEY_UNLOCKS)
            .map { board.indexOf(it.row, it.col) }
            .toSet()
        val opened = board.withCells(
            board.cells.map { cell ->
                val index = board.indexOf(cell.row, cell.col)
                if (targets.contains(index)) cell.copy(locked = false) else cell
            }
        )
        return JokerOutcome(
            board = opened,
            cagesBroken = targets.size,
            gemsCleared = 0,
            armsWildSwap = false
        )
    }
}
