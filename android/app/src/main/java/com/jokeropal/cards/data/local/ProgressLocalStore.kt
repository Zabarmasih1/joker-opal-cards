package com.jokeropal.cards.data.local

import android.content.Context
import android.content.SharedPreferences
import com.jokeropal.cards.core.config.GameConfig
import com.jokeropal.cards.data.sample.SampleData

class ProgressLocalStore(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)

    fun currentChapter(): Int = preferences.getInt(KEY_CHAPTER, SampleData.DEFAULT_CHAPTER)

    fun chaptersCleared(): Int =
        preferences.getInt(KEY_CLEARED, SampleData.DEFAULT_CHAPTERS_CLEARED)

    fun seals(): Int = preferences.getInt(KEY_SEALS, SampleData.DEFAULT_SEALS)

    fun bestCascade(): Int = preferences.getInt(KEY_CASCADE, SampleData.DEFAULT_BEST_CASCADE)

    fun chips(): Int = preferences.getInt(KEY_CHIPS, GameConfig.STARTING_CHIPS)

    fun earnedChapters(): Set<Int> {
        val stored = preferences.getStringSet(KEY_EARNED, null) ?: defaultEarned()
        return stored.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun store(
        chapter: Int,
        cleared: Int,
        seals: Int,
        bestCascade: Int,
        chips: Int,
        earned: Set<Int>
    ) {
        preferences.edit()
            .putInt(KEY_CHAPTER, chapter)
            .putInt(KEY_CLEARED, cleared)
            .putInt(KEY_SEALS, seals)
            .putInt(KEY_CASCADE, bestCascade)
            .putInt(KEY_CHIPS, chips)
            .putStringSet(KEY_EARNED, earned.map { it.toString() }.toSet())
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun defaultEarned(): Set<String> =
        (1..SampleData.DEFAULT_SEALS).map { it.toString() }.toSet()

    private companion object {
        const val STORE_NAME = "joker_opal_progress"
        const val KEY_CHAPTER = "chapter"
        const val KEY_CLEARED = "cleared"
        const val KEY_SEALS = "seals"
        const val KEY_CASCADE = "cascade"
        const val KEY_CHIPS = "chips"
        const val KEY_EARNED = "earned"
    }
}
