package com.jokeropal.cards.domain.repository

import com.jokeropal.cards.domain.model.ChapterDefinition

interface ChapterRepository {

    fun chapters(): List<ChapterDefinition>

    fun chapter(index: Int): ChapterDefinition
}
