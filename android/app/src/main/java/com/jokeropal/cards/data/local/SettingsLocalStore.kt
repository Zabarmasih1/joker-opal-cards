package com.jokeropal.cards.data.local

import android.content.Context
import android.content.SharedPreferences

class SettingsLocalStore(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)

    fun soundEnabled(): Boolean = preferences.getBoolean(KEY_SOUND, true)

    fun animationsEnabled(): Boolean = preferences.getBoolean(KEY_ANIMATIONS, true)

    fun highContrastGems(): Boolean = preferences.getBoolean(KEY_CONTRAST, false)

    fun store(sound: Boolean, animations: Boolean, contrast: Boolean) {
        preferences.edit()
            .putBoolean(KEY_SOUND, sound)
            .putBoolean(KEY_ANIMATIONS, animations)
            .putBoolean(KEY_CONTRAST, contrast)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val STORE_NAME = "joker_opal_settings"
        const val KEY_SOUND = "sound"
        const val KEY_ANIMATIONS = "animations"
        const val KEY_CONTRAST = "contrast"
    }
}
