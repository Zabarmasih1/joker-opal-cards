package com.jokeropal.cards.core.ui

import com.jokeropal.cards.R
import com.jokeropal.cards.domain.model.JokerType
import com.jokeropal.cards.domain.model.OpalGem

object GemAssets {

    fun drawableFor(gem: OpalGem): Int = when (gem) {
        OpalGem.VIOLET -> R.drawable.sprite_opal_violet
        OpalGem.GOLD -> R.drawable.sprite_opal_gold
        OpalGem.CRIMSON -> R.drawable.sprite_opal_crimson
        OpalGem.TEAL -> R.drawable.sprite_opal_teal
        OpalGem.PEARL -> R.drawable.sprite_opal_pearl
    }

    fun labelFor(joker: JokerType): Int = when (joker) {
        JokerType.WILD_SWAP -> R.string.joker_wild_swap
        JokerType.ROW_BURN -> R.string.joker_row_burn
        JokerType.CAGE_KEY -> R.string.joker_cage_key
    }
}
