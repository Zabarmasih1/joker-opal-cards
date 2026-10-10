package com.jokeropal.cards.domain.usecase

import com.jokeropal.cards.domain.model.ChapterResult
import com.jokeropal.cards.domain.repository.ProgressRepository

class SaveChapterResultUseCase(private val repository: ProgressRepository) {

    operator fun invoke(result: ChapterResult) {
        repository.save(result)
    }
}
