package com.jokeropal.cards.data.repository

import com.jokeropal.cards.core.config.GameConfig
import com.jokeropal.cards.data.local.ProgressLocalStore
import com.jokeropal.cards.data.sample.SampleData
import com.jokeropal.cards.domain.model.ChapterResult
import com.jokeropal.cards.domain.model.PlayerProgress
import com.jokeropal.cards.domain.model.SealRecord
import com.jokeropal.cards.domain.repository.ProgressRepository

class ProgressRepositoryImpl(private val store: ProgressLocalStore) : ProgressRepository {

    override fun progress(): PlayerProgress = PlayerProgress(
        currentChapter = store.currentChapter(),
        chaptersCleared = store.chaptersCleared(),
        seals = store.seals(),
        bestCascade = store.bestCascade(),
        chips = store.chips()
    )

    override fun seals(): List<SealRecord> {
        val earned = store.earnedChapters()
        return SampleData.chapters.map { chapter ->
            SealRecord(
                chapterIndex = chapter.index,
                chapterTitle = chapter.title,
                earned = earned.contains(chapter.index)
            )
        }
    }

    override fun save(result: ChapterResult) {
        val earned = store.earnedChapters().toMutableSet()
        var chapter = store.currentChapter()
        var cleared = store.chaptersCleared()
        if (result.won) {
            earned.add(result.chapterIndex)
            if (result.chapterIndex >= chapter) {
                chapter = (result.chapterIndex + 1).coerceAtMost(GameConfig.TOTAL_CHAPTERS)
            }
            cleared = earned.size
        }
        val cascade = maxOf(store.bestCascade(), result.bestCascade)
        val chips = store.chips() + result.chipsEarned
        store.store(
            chapter = chapter,
            cleared = cleared,
            seals = earned.size,
            bestCascade = cascade,
            chips = chips,
            earned = earned
        )
    }

    override fun reset() {
        store.clear()
        store.store(
            chapter = 1,
            cleared = 0,
            seals = 0,
            bestCascade = 0,
            chips = GameConfig.STARTING_CHIPS,
            earned = emptySet()
        )
    }
}
