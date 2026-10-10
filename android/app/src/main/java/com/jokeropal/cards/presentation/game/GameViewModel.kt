package com.jokeropal.cards.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jokeropal.cards.R
import com.jokeropal.cards.core.config.GameConfig
import com.jokeropal.cards.data.sample.SampleData
import com.jokeropal.cards.domain.model.BoardSnapshot
import com.jokeropal.cards.domain.model.ChapterDefinition
import com.jokeropal.cards.domain.model.ChapterOutcome
import com.jokeropal.cards.domain.model.ChapterResult
import com.jokeropal.cards.domain.model.JokerState
import com.jokeropal.cards.domain.model.JokerType
import com.jokeropal.cards.domain.model.SwapOutcome
import com.jokeropal.cards.domain.repository.ChapterRepository
import com.jokeropal.cards.domain.repository.ProgressRepository
import com.jokeropal.cards.domain.repository.SettingsRepository
import com.jokeropal.cards.domain.usecase.ApplyJokerUseCase
import com.jokeropal.cards.domain.usecase.EvaluateChapterUseCase
import com.jokeropal.cards.domain.usecase.GenerateBoardUseCase
import com.jokeropal.cards.domain.usecase.ResolveSwapUseCase
import com.jokeropal.cards.domain.usecase.SaveChapterResultUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val chapterRepository: ChapterRepository,
    private val progressRepository: ProgressRepository,
    private val settingsRepository: SettingsRepository,
    private val generateBoardUseCase: GenerateBoardUseCase,
    private val resolveSwapUseCase: ResolveSwapUseCase,
    private val applyJokerUseCase: ApplyJokerUseCase,
    private val evaluateChapterUseCase: EvaluateChapterUseCase,
    private val saveChapterResultUseCase: SaveChapterResultUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private var chapter: ChapterDefinition? = null
    private var board: BoardSnapshot? = null
    private var jokers: List<JokerState> = emptyList()

    private var moves = 0
    private var score = 0
    private var cascade = 0
    private var bestCascade = 0
    private var cagesTotal = 0
    private var cagesBroken = 0
    private var selected = -1
    private var banner = 0
    private var wildArmed = false
    private var busy = false
    private var terminal = false
    private var highContrast = false
    private var animationsEnabled = true

    fun start() {
        if (chapter != null) return
        viewModelScope.launch {
            val progress = progressRepository.progress()
            val definition = chapterRepository.chapter(progress.currentChapter)
            val settings = settingsRepository.settings()
            highContrast = settings.highContrastGems
            animationsEnabled = settings.animationsEnabled
            val dealt = generateBoardUseCase(definition)
            chapter = definition
            board = dealt
            moves = definition.moveBudget
            cagesTotal = dealt.lockedCount
            cagesBroken = 0
            score = 0
            cascade = 0
            bestCascade = 0
            selected = -1
            jokers = SampleData.jokers.map { JokerState(it, 0) }
            banner = R.string.game_banner_dealing
            publish(GameUiState.Phase.DEALING, animate = true)
            delay(DEAL_HOLD_MS)
            banner = 0
            publish(GameUiState.Phase.IDLE, animate = false)
        }
    }

    fun refreshSettings() {
        viewModelScope.launch {
            val settings = settingsRepository.settings()
            highContrast = settings.highContrastGems
            animationsEnabled = settings.animationsEnabled
            val current = _state.value
            if (current is GameUiState.Playing) {
                publish(current.phase, animate = false)
            }
        }
    }

    fun onCellTap(row: Int, col: Int) {
        val current = board ?: return
        if (busy || terminal) return
        if (!current.contains(row, col)) return
        val index = current.indexOf(row, col)
        if (current.cellAt(index).locked) return
        if (selected == -1 || selected == index) {
            selected = if (selected == index) -1 else index
            publish(
                if (selected == -1) GameUiState.Phase.IDLE else GameUiState.Phase.SELECTED,
                animate = false
            )
            return
        }
        val adjacent = isAdjacent(selected, index, current.cols)
        if (!adjacent && !wildArmed) {
            selected = index
            publish(GameUiState.Phase.SELECTED, animate = false)
            return
        }
        val origin = selected
        selected = -1
        swapCells(origin, index)
    }

    fun onJokerTapped(type: JokerType) {
        val ready = jokers.firstOrNull { it.type == type && it.isReady } ?: return
        fireJoker(ready.type)
    }

    fun onPlayJoker() {
        val ready = jokers.firstOrNull { it.isReady } ?: return
        fireJoker(ready.type)
    }

    private fun swapCells(first: Int, second: Int) {
        val current = board ?: return
        busy = true
        viewModelScope.launch {
            val forced = wildArmed
            val outcome = resolveSwapUseCase(current, first, second, forced)
            if (!outcome.legal) {
                board = outcome.swappedBoard
                publish(GameUiState.Phase.RESOLVING, animate = true)
                delay(GameConfig.REVERT_ANIM_MS)
                board = current
                publish(GameUiState.Phase.IDLE, animate = true)
                busy = false
                return@launch
            }
            if (forced) wildArmed = false
            moves = (moves - 1).coerceAtLeast(0)
            board = outcome.swappedBoard
            publish(GameUiState.Phase.RESOLVING, animate = true)
            delay(GameConfig.SWAP_ANIM_MS)
            applySteps(outcome)
            finishTurn()
        }
    }

    private suspend fun applySteps(outcome: SwapOutcome) {
        var index = 0
        while (index < outcome.steps.size) {
            val step = outcome.steps[index]
            board = step.board
            score += step.score
            cagesBroken += step.cagesBroken
            cascade = step.cascadeIndex + 1
            if (cascade > bestCascade) bestCascade = cascade
            chargeJokers(step.gemsCleared)
            publish(GameUiState.Phase.RESOLVING, animate = true)
            delay(GameConfig.CLEAR_ANIM_MS + GameConfig.FALL_ANIM_MS)
            index++
        }
    }

    private fun fireJoker(type: JokerType) {
        val current = board ?: return
        if (busy || terminal) return
        busy = true
        viewModelScope.launch {
            publish(GameUiState.Phase.JOKER_FIRING, animate = false)
            delay(JOKER_REVEAL_MS)
            val outcome = applyJokerUseCase(current, type)
            board = outcome.board
            cagesBroken += outcome.cagesBroken
            score += outcome.gemsCleared * GameConfig.SCORE_PER_GEM +
                outcome.cagesBroken * GameConfig.SCORE_PER_CAGE
            wildArmed = outcome.armsWildSwap
            banner = if (outcome.armsWildSwap) R.string.game_banner_wild_armed else 0
            jokers = jokers.map { joker ->
                if (joker.type == type) joker.copy(charge = 0) else joker
            }
            publish(GameUiState.Phase.RESOLVING, animate = true)
            delay(GameConfig.FALL_ANIM_MS)
            val follow = resolveSwapUseCase.resolve(board ?: outcome.board)
            var index = 0
            while (index < follow.size) {
                val step = follow[index]
                board = step.board
                score += step.score
                cagesBroken += step.cagesBroken
                cascade = step.cascadeIndex + 1
                if (cascade > bestCascade) bestCascade = cascade
                chargeJokers(step.gemsCleared)
                publish(GameUiState.Phase.RESOLVING, animate = true)
                delay(GameConfig.CLEAR_ANIM_MS + GameConfig.FALL_ANIM_MS)
                index++
            }
            finishTurn()
        }
    }

    private suspend fun finishTurn() {
        val current = board
        if (current == null) {
            busy = false
            return
        }
        if (!resolveSwapUseCase.hasLegalMove(current)) {
            banner = R.string.game_banner_reshuffle
            val shuffled = generateBoardUseCase.reshuffle(current)
            board = shuffled
            publish(GameUiState.Phase.RESOLVING, animate = true)
            delay(GameConfig.BOARD_RESHUFFLE_MS)
            banner = 0
        }
        val outcome = evaluateChapterUseCase(board ?: current, moves)
        if (outcome == ChapterOutcome.ONGOING) {
            publish(GameUiState.Phase.IDLE, animate = false)
            busy = false
            return
        }
        terminal = true
        publish(GameUiState.Phase.TERMINAL, animate = false)
        delay(GameConfig.ROUND_END_HOLD_MS)
        val definition = chapter
        val result = ChapterResult(
            chapterIndex = definition?.index ?: 1,
            chapterTitle = definition?.title ?: SampleData.titleFor(1),
            won = outcome == ChapterOutcome.WON,
            score = score,
            cagesBroken = cagesBroken,
            cagesTotal = cagesTotal,
            bestCascade = bestCascade,
            chipsEarned = if (outcome == ChapterOutcome.WON) GameConfig.CHIPS_REWARD_WIN else 0
        )
        saveChapterResultUseCase(result)
        _state.value = GameUiState.Finished(result)
        busy = false
    }

    private fun chargeJokers(gems: Int) {
        if (gems <= 0) return
        val gain = gems * GameConfig.JOKER_CHARGE_PER_GEM
        val updated = ArrayList<JokerState>(jokers.size)
        var applied = false
        for (joker in jokers) {
            if (!applied && !joker.isReady) {
                applied = true
                val charge = (joker.charge + gain).coerceAtMost(GameConfig.JOKER_CHARGE_FULL)
                updated.add(joker.copy(charge = charge))
            } else {
                updated.add(joker)
            }
        }
        jokers = updated
    }

    private fun isAdjacent(first: Int, second: Int, cols: Int): Boolean {
        val rowDelta = first / cols - second / cols
        val colDelta = first % cols - second % cols
        val rows = if (rowDelta < 0) -rowDelta else rowDelta
        val columns = if (colDelta < 0) -colDelta else colDelta
        return rows + columns == 1
    }

    private fun publish(phase: GameUiState.Phase, animate: Boolean) {
        val current = board ?: return
        val definition = chapter ?: return
        _state.value = GameUiState.Playing(
            phase = phase,
            chapterIndex = definition.index,
            chapterTitle = definition.title,
            board = current,
            selected = selected,
            moves = moves,
            score = score,
            cascade = cascade,
            cagesBroken = cagesBroken,
            cagesTotal = cagesTotal,
            jokers = jokers,
            bannerText = banner,
            animate = animate && animationsEnabled,
            highContrast = highContrast
        )
    }

    private companion object {
        const val DEAL_HOLD_MS = 620L
        const val JOKER_REVEAL_MS = 380L
    }
}
