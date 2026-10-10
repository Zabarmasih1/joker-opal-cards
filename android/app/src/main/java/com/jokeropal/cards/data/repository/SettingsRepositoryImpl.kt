package com.jokeropal.cards.data.repository

import com.jokeropal.cards.data.local.SettingsLocalStore
import com.jokeropal.cards.domain.model.AppSettings
import com.jokeropal.cards.domain.repository.SettingsRepository

class SettingsRepositoryImpl(private val store: SettingsLocalStore) : SettingsRepository {

    override fun settings(): AppSettings = AppSettings(
        soundEnabled = store.soundEnabled(),
        animationsEnabled = store.animationsEnabled(),
        highContrastGems = store.highContrastGems()
    )

    override fun update(settings: AppSettings) {
        store.store(
            sound = settings.soundEnabled,
            animations = settings.animationsEnabled,
            contrast = settings.highContrastGems
        )
    }
}
