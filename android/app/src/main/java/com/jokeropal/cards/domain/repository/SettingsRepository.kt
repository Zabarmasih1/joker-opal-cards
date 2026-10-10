package com.jokeropal.cards.domain.repository

import com.jokeropal.cards.domain.model.AppSettings

interface SettingsRepository {

    fun settings(): AppSettings

    fun update(settings: AppSettings)
}
