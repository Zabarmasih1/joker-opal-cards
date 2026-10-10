package com.jokeropal.cards.data.repository

import com.jokeropal.cards.data.sample.SampleData
import com.jokeropal.cards.domain.model.ChapterDefinition
import com.jokeropal.cards.domain.repository.ChapterRepository

class ChapterRepositoryImpl : ChapterRepository {

    override fun chapters(): List<ChapterDefinition> = SampleData.chapters

    override fun chapter(index: Int): ChapterDefinition {
        val catalogue = SampleData.chapters
        val slot = (index - 1).coerceIn(0, catalogue.size - 1)
        return catalogue[slot]
    }
}
