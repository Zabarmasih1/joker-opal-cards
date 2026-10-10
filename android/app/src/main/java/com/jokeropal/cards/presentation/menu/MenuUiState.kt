package com.jokeropal.cards.presentation.menu

data class MenuUiState(
    val chapterIndex: Int,
    val chapterTitle: String,
    val chaptersCleared: Int,
    val totalChapters: Int,
    val seals: Int,
    val bestCascade: Int,
    val chips: Int
) {

    val statsVisible: Boolean
        get() = seals > 0 && bestCascade > 0
}
