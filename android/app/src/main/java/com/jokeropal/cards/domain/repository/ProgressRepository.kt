package com.jokeropal.cards.domain.repository

import com.jokeropal.cards.domain.model.ChapterResult
import com.jokeropal.cards.domain.model.PlayerProgress
import com.jokeropal.cards.domain.model.SealRecord

interface ProgressRepository {

    fun progress(): PlayerProgress

    fun seals(): List<SealRecord>

    fun save(result: ChapterResult)

    fun reset()
}
