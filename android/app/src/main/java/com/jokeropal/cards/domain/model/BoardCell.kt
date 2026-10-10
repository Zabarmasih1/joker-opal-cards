package com.jokeropal.cards.domain.model

data class BoardCell(
    val row: Int,
    val col: Int,
    val gem: OpalGem,
    val locked: Boolean,
    val wild: Boolean
)
