package com.jokeropal.cards.domain.model

data class BoardSnapshot(
    val rows: Int,
    val cols: Int,
    val cells: List<BoardCell>
) {

    fun indexOf(row: Int, col: Int): Int = row * cols + col

    fun cellAt(row: Int, col: Int): BoardCell = cells[indexOf(row, col)]

    fun cellAt(index: Int): BoardCell = cells[index]

    fun contains(row: Int, col: Int): Boolean = row in 0 until rows && col in 0 until cols

    val lockedCount: Int
        get() = cells.count { it.locked }

    fun withCells(updated: List<BoardCell>): BoardSnapshot = copy(cells = updated)
}
