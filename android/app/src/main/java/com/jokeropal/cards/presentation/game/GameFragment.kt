package com.jokeropal.cards.presentation.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jokeropal.cards.R
import com.jokeropal.cards.core.di.AppViewModelFactory
import com.jokeropal.cards.core.navigation.Navigator
import com.jokeropal.cards.core.ui.JokerCardView
import com.jokeropal.cards.core.ui.OpalBoardView
import com.jokeropal.cards.core.ui.PressFeedback
import com.jokeropal.cards.core.ui.ViewExt
import com.jokeropal.cards.databinding.FragmentGameBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private var animator: BoardAnimator? = null
    private var navigated = false

    private val viewModel: GameViewModel by viewModels {
        AppViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentGameBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = _binding ?: return

        val boardAnimator = BoardAnimator(binding)
        boardAnimator.onScoreTick = { amount -> renderScore(amount) }
        animator = boardAnimator

        binding.viewOpalBoard.tapListener = object : OpalBoardView.OnCellTapListener {
            override fun onCellTap(row: Int, col: Int) {
                viewModel.onCellTap(row, col)
            }
        }

        binding.buttonGameBack.setOnClickListener { target ->
            PressFeedback.pulse(target)
            leaveGame()
        }

        binding.buttonPlayJoker.setOnClickListener { target ->
            PressFeedback.pulse(target)
            viewModel.onPlayJoker()
        }

        bindJoker(binding.viewJokerFirst)
        bindJoker(binding.viewJokerSecond)
        bindJoker(binding.viewJokerThird)

        observeState()
        viewModel.start()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshSettings()
    }

    private fun bindJoker(card: JokerCardView) {
        card.setOnClickListener { target ->
            val joker = card.type() ?: return@setOnClickListener
            PressFeedback.pulse(target)
            card.playReveal()
            viewModel.onJokerTapped(joker)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state -> render(state) }
            }
        }
    }

    private fun render(state: GameUiState) {
        when (state) {
            is GameUiState.Loading -> return
            is GameUiState.Playing -> renderPlaying(state)
            is GameUiState.Finished -> openResult(state)
        }
    }

    private fun renderPlaying(state: GameUiState.Playing) {
        val binding = _binding ?: return
        val context = binding.root.context

        binding.textGameChapterIndex.text =
            getString(R.string.fmt_chapter_heading, state.chapterIndex)
        binding.textGameChapterTitle.text = state.chapterTitle

        binding.textMovesValue.text = state.moves.toString()
        val lowMoves = state.moves <= LOW_MOVES_THRESHOLD
        val movesColor = ContextCompat.getColor(
            context,
            if (lowMoves) R.color.secondary_crimson else R.color.accent_gold
        )
        binding.textMovesValue.setTextColor(movesColor)
        ViewExt.describeState(
            binding.cardMoves,
            getString(R.string.fmt_moves_state, state.moves)
        )
        animator?.pulseMoves(lowMoves)

        binding.viewOpalBoard.render(
            state.board,
            state.selected,
            state.highContrast,
            state.animate
        )
        binding.viewOpalBoard.isEnabled = state.interactive

        binding.statGameCages.bind(
            R.drawable.ic_lock,
            getString(R.string.fmt_cages, state.cagesBroken, state.cagesTotal),
            getString(R.string.label_cages),
            ContextCompat.getColor(context, R.color.secondary_crimson)
        )
        binding.statGameCascade.bind(
            R.drawable.ic_cascade,
            if (state.cascade > 0) {
                getString(R.string.fmt_cascade, state.cascade)
            } else {
                getString(R.string.empty_value)
            },
            getString(R.string.label_cascade),
            ContextCompat.getColor(context, R.color.cascade_text)
        )
        animator?.animateScore(state.score)

        val jokerViews = listOf(
            binding.viewJokerFirst,
            binding.viewJokerSecond,
            binding.viewJokerThird
        )
        for (position in jokerViews.indices) {
            val joker = state.jokers.getOrNull(position) ?: continue
            jokerViews[position].bind(joker)
        }

        binding.buttonPlayJoker.isEnabled = state.jokerReady && state.interactive
        ViewExt.describeState(
            binding.buttonPlayJoker,
            if (state.jokerReady) {
                getString(R.string.game_cta_joker)
            } else {
                getString(R.string.joker_none_ready)
            }
        )

        val banner = if (state.bannerText != 0) getString(state.bannerText) else null
        animator?.showBanner(banner)
    }

    private fun renderScore(amount: Int) {
        val binding = _binding ?: return
        val context = binding.root.context
        binding.statGameScore.bind(
            R.drawable.ic_chip,
            getString(R.string.fmt_score, amount),
            getString(R.string.label_score),
            ContextCompat.getColor(context, R.color.accent_gold)
        )
    }

    private fun openResult(state: GameUiState.Finished) {
        if (navigated) return
        if (!isAdded || view == null) return
        navigated = true
        try {
            Navigator.toResult(parentFragmentManager, state.result)
        } catch (e: Exception) {
            navigated = false
        }
    }

    private fun leaveGame() {
        if (!isAdded) return
        try {
            Navigator.back(parentFragmentManager)
        } catch (e: Exception) {
            return
        }
    }

    override fun onDestroyView() {
        val binding = _binding
        animator?.cancel()
        animator = null
        if (binding != null) {
            binding.viewOpalBoard.tapListener = null
            binding.viewOpalBoard.clearAnimations()
            PressFeedback.release(binding.buttonPlayJoker)
            PressFeedback.release(binding.buttonGameBack)
            PressFeedback.release(binding.viewJokerFirst)
            PressFeedback.release(binding.viewJokerSecond)
            PressFeedback.release(binding.viewJokerThird)
        }
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val LOW_MOVES_THRESHOLD = 5
    }
}
