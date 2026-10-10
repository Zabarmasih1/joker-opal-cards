package com.jokeropal.cards.presentation.gameover

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jokeropal.cards.R
import com.jokeropal.cards.core.di.AppViewModelFactory
import com.jokeropal.cards.core.navigation.Navigator
import com.jokeropal.cards.core.ui.PressFeedback
import com.jokeropal.cards.core.ui.ViewExt
import com.jokeropal.cards.databinding.FragmentGameOverBinding
import com.jokeropal.cards.domain.model.ChapterResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameOverFragment : Fragment() {

    private var _binding: FragmentGameOverBinding? = null
    private var heroIdle: ValueAnimator? = null

    private val viewModel: GameOverViewModel by viewModels {
        AppViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentGameOverBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = _binding ?: return

        binding.buttonPlayAgain.setOnClickListener { target ->
            PressFeedback.pulse(target)
            replay()
        }
        binding.buttonResultMenu.setOnClickListener { target ->
            PressFeedback.pulse(target)
            backToMenu()
        }

        observeState()
        viewModel.bind(readResult())
    }

    private fun readResult(): ChapterResult {
        val args = arguments
        return ChapterResult(
            chapterIndex = args?.getInt(ARG_CHAPTER_INDEX) ?: 1,
            chapterTitle = args?.getString(ARG_CHAPTER_TITLE).orEmpty(),
            won = args?.getBoolean(ARG_WON) ?: false,
            score = args?.getInt(ARG_SCORE) ?: 0,
            cagesBroken = args?.getInt(ARG_CAGES_BROKEN) ?: 0,
            cagesTotal = args?.getInt(ARG_CAGES_TOTAL) ?: 0,
            bestCascade = args?.getInt(ARG_BEST_CASCADE) ?: 0,
            chipsEarned = args?.getInt(ARG_CHIPS) ?: 0
        )
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    if (state != null) render(state)
                }
            }
        }
    }

    private fun render(state: GameOverUiState) {
        val binding = _binding ?: return
        val context = binding.root.context

        val accent = ContextCompat.getColor(
            context,
            if (state.won) R.color.accent_gold else R.color.secondary_crimson
        )

        binding.viewResultScrim.setBackgroundResource(
            if (state.won) R.drawable.bg_result_scrim_win else R.drawable.bg_result_scrim_loss
        )
        binding.textResultTitle.setText(
            if (state.won) R.string.result_title_win else R.string.result_title_loss
        )
        binding.textResultTitle.setTextColor(accent)
        binding.textResultSubtitle.text = if (state.won) {
            getString(R.string.fmt_result_subtitle_win, state.chapterTitle)
        } else {
            getString(R.string.result_subtitle_loss)
        }

        if (state.won) {
            binding.imageResultHero.setImageResource(R.drawable.sprite_seal_gold)
            binding.imageResultHero.contentDescription = getString(R.string.result_seal_cd)
        } else {
            binding.imageResultHero.setImageResource(R.drawable.sprite_lock_cage)
            binding.imageResultHero.contentDescription = getString(R.string.result_cage_cd)
        }

        binding.statResultScore.bind(
            R.drawable.ic_chip,
            getString(R.string.fmt_score, state.score),
            getString(R.string.label_score),
            ContextCompat.getColor(context, R.color.accent_gold)
        )
        binding.statResultCages.bind(
            R.drawable.ic_lock,
            getString(R.string.fmt_cages, state.cagesBroken, state.cagesTotal),
            getString(R.string.label_cages_broken),
            ContextCompat.getColor(context, R.color.secondary_crimson)
        )
        binding.statResultCascade.bind(
            R.drawable.ic_cascade,
            getString(R.string.fmt_cascade, state.bestCascade),
            getString(R.string.label_best_cascade),
            ContextCompat.getColor(context, R.color.cascade_text)
        )

        ViewExt.show(binding.layoutResultReward, state.rewardVisible)
        ViewExt.show(binding.viewResultDivider, state.rewardVisible)
        if (state.rewardVisible) {
            binding.textResultReward.text =
                getString(R.string.fmt_chips_reward, state.chipsEarned)
        }

        playEntrance(state.won)
    }

    private fun playEntrance(won: Boolean) {
        val binding = _binding ?: return
        val hero = binding.imageResultHero
        hero.scaleX = 0.6f
        hero.scaleY = 0.6f
        hero.rotation = if (won) -14f else 0f
        try {
            hero.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0f)
                .setDuration(HERO_MS)
                .setInterpolator(OvershootInterpolator(1.3f))
                .withEndAction {
                    try {
                        if (!isAdded || view == null) return@withEndAction
                        if (won) startHeroIdle()
                    } catch (e: Exception) {
                        return@withEndAction
                    }
                }
                .start()
        } catch (e: Exception) {
            hero.scaleX = 1f
            hero.scaleY = 1f
            hero.rotation = 0f
        }

        ViewExt.fadeIn(binding.cardResult, 120L, CARD_RISE)
        ViewExt.fadeIn(binding.buttonPlayAgain, 260L, CARD_RISE)
        ViewExt.fadeIn(binding.buttonResultMenu, 320L, CARD_RISE)
    }

    private fun startHeroIdle() {
        if (heroIdle != null) return
        val binding = _binding ?: return
        val animator = ValueAnimator.ofFloat(-3f, 3f)
        animator.duration = IDLE_MS
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener { value ->
            try {
                val hero = _binding?.imageResultHero ?: return@addUpdateListener
                if (!hero.isAttachedToWindow) return@addUpdateListener
                hero.rotation = value.animatedValue as Float
            } catch (e: Exception) {
                return@addUpdateListener
            }
        }
        animator.start()
        heroIdle = animator
        binding.viewResultGlow.alpha = 0.9f
    }

    private fun replay() {
        if (!isAdded) return
        try {
            Navigator.replayChapter(parentFragmentManager)
        } catch (e: Exception) {
            return
        }
    }

    private fun backToMenu() {
        if (!isAdded) return
        try {
            Navigator.backToMenu(parentFragmentManager)
        } catch (e: Exception) {
            return
        }
    }

    override fun onDestroyView() {
        try {
            heroIdle?.cancel()
        } catch (e: Exception) {
            heroIdle = null
        }
        heroIdle = null
        val binding = _binding
        if (binding != null) {
            ViewExt.cancelAnimation(binding.imageResultHero)
            ViewExt.cancelAnimation(binding.cardResult)
            PressFeedback.release(binding.buttonPlayAgain)
            PressFeedback.release(binding.buttonResultMenu)
        }
        _binding = null
        super.onDestroyView()
    }

    companion object {

        private const val ARG_CHAPTER_INDEX = "chapter_index"
        private const val ARG_CHAPTER_TITLE = "chapter_title"
        private const val ARG_WON = "won"
        private const val ARG_SCORE = "score"
        private const val ARG_CAGES_BROKEN = "cages_broken"
        private const val ARG_CAGES_TOTAL = "cages_total"
        private const val ARG_BEST_CASCADE = "best_cascade"
        private const val ARG_CHIPS = "chips"
        private const val HERO_MS = 520L
        private const val IDLE_MS = 2400L
        private const val CARD_RISE = 40f

        fun newInstance(result: ChapterResult): GameOverFragment {
            val fragment = GameOverFragment()
            val args = Bundle()
            args.putInt(ARG_CHAPTER_INDEX, result.chapterIndex)
            args.putString(ARG_CHAPTER_TITLE, result.chapterTitle)
            args.putBoolean(ARG_WON, result.won)
            args.putInt(ARG_SCORE, result.score)
            args.putInt(ARG_CAGES_BROKEN, result.cagesBroken)
            args.putInt(ARG_CAGES_TOTAL, result.cagesTotal)
            args.putInt(ARG_BEST_CASCADE, result.bestCascade)
            args.putInt(ARG_CHIPS, result.chipsEarned)
            fragment.arguments = args
            return fragment
        }
    }
}
