package com.jokeropal.cards.domain.model

enum class JokerType(val title: String, val summary: String) {
    WILD_SWAP("WILD SWAP", "The next swap is always legal"),
    ROW_BURN("ROW BURN", "Burns the most caged row"),
    CAGE_KEY("CAGE KEY", "Opens up to three cages")
}
