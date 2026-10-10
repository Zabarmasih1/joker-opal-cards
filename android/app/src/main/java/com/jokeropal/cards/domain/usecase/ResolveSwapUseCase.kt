package com.jokeropal.cards.domain.usecase

import com.jokeropal.cards.core.config.GameConfig
import com.jokeropal.cards.domain.model.BoardCell
import com.jokeropal.cards.domain.model.BoardSnapshot
import com.jokeropal.cards.domain.model.OpalGem
import com.jokeropal.cards.domain.model.ResolutionStep
import com.jokeropal.cards.domain.model.SwapOutcome
import kotlin.random.Random

class ResolveSwapUseCase(private val random: Random = Random.Default) {

    operator fun invoke(board: BoardSnapshot, first: Int, second: Int, forced: Boolean): SwapOutcome {
        val swapped = swap(board, first, second)
        val steps = resolve(swapped)
        val legal = forced || steps.isNotEmpty()
        return SwapOutcome(legal = legal, swappedBoard = swapped, steps = if (legal) steps else emptyList())
    }

    fun swap(board: BoardSnapshot, first: Int, second: Int): BoardSnapshot {
        val cells = board.cells.toMutableList()
        val a = cells[first]
        val b = cells[second]
        cells[first] = a.copy(gem = b.gem, wild = b.wild)
        cells[second] = b.copy(gem = a.gem, wild = a.wild)
        return board.withCells(cells)
    }

    fun hasLegalMove(board: BoardSnapshot): Boolean {
        for (row in 0 until board.rows) {
            for (col in 0 until board.cols) {
                val index = board.indexOf(row, col)
                if (board.cellAt(index).locked) continue
                if (col + 1 < board.cols) {
                    val right = board.indexOf(row, col + 1)
                    if (!board.cellAt(right).locked && findMatches(swap(board, index, right)).isNotEmpty()) {
                        return true
                    }
                }
                if (row + 1 < board.rows) {
                    val down = board.indexOf(row + 1, col)
                    if (!board.cellAt(down).locked && findMatches(swap(board, index, down)).isNotEmpty()) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun resolve(start: BoardSnapshot): List<ResolutionStep> {
        val steps = ArrayList<ResolutionStep>()
        var board = start
        var cascade = 0
        while (cascade < MAX_CASCADES) {
            val matched = findMatches(board)
            if (matched.isEmpty()) break
            val cages = cagesTouchedBy(board, matched)
            val clearable = matched.filterNot { board.cellAt(it).locked }.toSet()
            val opened = board.withCells(
                board.cells.map { cell ->
                    val index = board.indexOf(cell.row, cell.col)
                    if (cages.contains(index)) cell.copy(locked = false) else cell
                }
            )
            board = collapse(opened, clearable)
            val gained = GameConfig.SCORE_PER_GEM * clearable.size * (cascade + 1) +
                GameConfig.SCORE_PER_CAGE * cages.size
            steps.add(
                ResolutionStep(
                    cascadeIndex = cascade,
                    gemsCleared = clearable.size,
                    cagesBroken = cages.size,
                    score = gained,
                    board = board
                )
            )
            cascade++
        }
        return steps
    }

    fun findMatches(board: BoardSnapshot): Set<Int> {
        val marked = LinkedHashSet<Int>()
        for (row in 0 until board.rows) {
            val line = ArrayList<Int>(board.cols)
            for (col in 0 until board.cols) line.add(board.indexOf(row, col))
            scanLine(board, line, marked)
        }
        for (col in 0 until board.cols) {
            val line = ArrayList<Int>(board.rows)
            for (row in 0 until board.rows) line.add(board.indexOf(row, col))
            scanLine(board, line, marked)
        }
        return marked
    }

    private fun scanLine(board: BoardSnapshot, line: List<Int>, out: MutableSet<Int>) {
        var start = 0
        while (start < line.size) {
            var end = start
            var gem: OpalGem? = if (board.cellAt(line[start]).wild) null else board.cellAt(line[start]).gem
            while (end + 1 < line.size) {
                val next = board.cellAt(line[end + 1])
                if (next.wild) {
                    end++
                    continue
                }
                if (gem == null) {
                    gem = next.gem
                    end++
                    continue
                }
                if (next.gem == gem) {
                    end++
                    continue
                }
                break
            }
            if (gem != null && end - start + 1 >= MIN_RUN) {
                for (position in start..end) out.add(line[position])
            }
            start = if (end > start) end + 1 else start + 1
        }
    }

    private fun cagesTouchedBy(board: BoardSnapshot, matched: Set<Int>): Set<Int> {
        val cages = LinkedHashSet<Int>()
        for (index in matched) {
            val cell = board.cellAt(index)
            if (cell.locked) cages.add(index)
            addCage(board, cell.row - 1, cell.col, cages)
            addCage(board, cell.row + 1, cell.col, cages)
            addCage(board, cell.row, cell.col - 1, cages)
            addCage(board, cell.row, cell.col + 1, cages)
        }
        return cages
    }

    private fun addCage(board: BoardSnapshot, row: Int, col: Int, out: MutableSet<Int>) {
        if (!board.contains(row, col)) return
        val index = board.indexOf(row, col)
        if (board.cellAt(index).locked) out.add(index)
    }

    fun collapse(board: BoardSnapshot, cleared: Set<Int>): BoardSnapshot {
        if (cleared.isEmpty()) return board
        val palette = OpalGem.values().take(GameConfig.GEM_TYPES)
        val cells = board.cells.toMutableList()
        for (col in 0 until board.cols) {
            var segmentBottom = board.rows - 1
            var row = board.rows - 1
            while (row >= -1) {
                val isBarrier = row < 0 || cells[board.indexOf(row, col)].locked
                if (isBarrier) {
                    compactSegment(board, cells, col, row + 1, segmentBottom, cleared, palette)
                    segmentBottom = row - 1
                }
                row--
            }
        }
        return board.withCells(cells)
    }

    private fun compactSegment(
        board: BoardSnapshot,
        cells: MutableList<BoardCell>,
        col: Int,
        top: Int,
        bottom: Int,
        cleared: Set<Int>,
        palette: List<OpalGem>
    ) {
        if (top > bottom) return
        val survivors = ArrayList<BoardCell>()
        for (row in bottom downTo top) {
            val index = board.indexOf(row, col)
            if (!cleared.contains(index)) survivors.add(cells[index])
        }
        var cursor = 0
        for (row in bottom downTo top) {
            val index = board.indexOf(row, col)
            val template = cells[index]
            val replacement = if (cursor < survivors.size) {
                val source = survivors[cursor]
                template.copy(gem = source.gem, wild = source.wild)
            } else {
                template.copy(gem = palette[random.nextInt(palette.size)], wild = false)
            }
            cells[index] = replacement
            cursor++
        }
    }

    private companion object {
        const val MIN_RUN = 3
        const val MAX_CASCADES = 12
    }
}
