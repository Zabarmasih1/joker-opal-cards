package com.jokeropal.cards.core.di

import android.content.Context
import com.jokeropal.cards.data.local.ProgressLocalStore
import com.jokeropal.cards.data.local.SettingsLocalStore
import com.jokeropal.cards.data.repository.ChapterRepositoryImpl
import com.jokeropal.cards.data.repository.ProgressRepositoryImpl
import com.jokeropal.cards.data.repository.SettingsRepositoryImpl
import com.jokeropal.cards.domain.repository.ChapterRepository
import com.jokeropal.cards.domain.repository.ProgressRepository
import com.jokeropal.cards.domain.repository.SettingsRepository
import com.jokeropal.cards.domain.usecase.ApplyJokerUseCase
import com.jokeropal.cards.domain.usecase.EvaluateChapterUseCase
import com.jokeropal.cards.domain.usecase.GenerateBoardUseCase
import com.jokeropal.cards.domain.usecase.ResolveSwapUseCase
import com.jokeropal.cards.domain.usecase.SaveChapterResultUseCase

object ServiceLocator {

    private var appContext: Context? = null
    private var progressRepository: ProgressRepository? = null
    private var chapterRepository: ChapterRepository? = null
    private var settingsRepository: SettingsRepository? = null
    private var resolveSwapUseCase: ResolveSwapUseCase? = null

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    fun progress(context: Context): ProgressRepository {
        init(context)
        val existing = progressRepository
        if (existing != null) return existing
        val created = ProgressRepositoryImpl(ProgressLocalStore(requireContext(context)))
        progressRepository = created
        return created
    }

    fun chapters(context: Context): ChapterRepository {
        init(context)
        val existing = chapterRepository
        if (existing != null) return existing
        val created = ChapterRepositoryImpl()
        chapterRepository = created
        return created
    }

    fun settings(context: Context): SettingsRepository {
        init(context)
        val existing = settingsRepository
        if (existing != null) return existing
        val created = SettingsRepositoryImpl(SettingsLocalStore(requireContext(context)))
        settingsRepository = created
        return created
    }

    fun resolveSwap(): ResolveSwapUseCase {
        val existing = resolveSwapUseCase
        if (existing != null) return existing
        val created = ResolveSwapUseCase()
        resolveSwapUseCase = created
        return created
    }

    fun generateBoard(): GenerateBoardUseCase = GenerateBoardUseCase()

    fun applyJoker(): ApplyJokerUseCase = ApplyJokerUseCase(resolveSwap())

    fun evaluateChapter(): EvaluateChapterUseCase = EvaluateChapterUseCase()

    fun saveChapterResult(context: Context): SaveChapterResultUseCase =
        SaveChapterResultUseCase(progress(context))

    private fun requireContext(fallback: Context): Context = appContext ?: fallback.applicationContext
}
