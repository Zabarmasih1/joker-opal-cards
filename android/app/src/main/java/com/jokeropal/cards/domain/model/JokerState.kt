package com.jokeropal.cards.domain.model

import com.jokeropal.cards.core.config.GameConfig

data class JokerState(
    val type: JokerType,
    val charge: Int
) {

    val isReady: Boolean
        get() = charge >= GameConfig.JOKER_CHARGE_FULL

    val percent: Int
        get() = (charge * 100 / GameConfig.JOKER_CHARGE_FULL).coerceIn(0, 100)
}
