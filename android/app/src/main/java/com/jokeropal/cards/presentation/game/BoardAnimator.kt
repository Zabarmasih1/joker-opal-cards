package com.jokeropal.cards.presentation.game

import android.animation.ValueAnimator
import android.view.View
import com.jokeropal.cards.databinding.FragmentGameBinding

class BoardAnimator(private val binding: FragmentGameBinding) {

    private var countUp: ValueAnimator? = null
    private var lowMoves: ValueAnimator? = null
    private var shownScore = 0

    fun animateScore(target: Int) {
        if (target == shownScore) {
            onScoreTick?.invoke(target)
            return
        }
        val from = shownScore
        shownScore = target
        try {
            countUp?.cancel()
        } catch (e: Exception) {
            countUp = null
        }
        val animator = ValueAnimator.ofInt(from, target)
        animator.duration = SCORE_MS
        animator.addUpdateListener { value ->
            try {
                if (!binding.layoutGameStrip.isAttachedToWindow) return@addUpdateListener
                val amount = value.animatedValue as Int
                onScoreTick?.invoke(amount)
            } catch (e: Exception) {
                return@addUpdateListener
            }
        }
        animator.start()
        countUp = animator
    }

    var onScoreTick: ((Int) -> Unit)? = null

    fun pulseMoves(low: Boolean) {
        if (!low) {
            stopLowMoves()
            return
        }
        if (lowMoves != null) return
        val animator = ValueAnimator.ofFloat(1f, 1.08f)
        animator.duration = LOW_MOVES_MS
        animator.repeatCount = LOW_MOVES_CYCLES
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener { value ->
            try {
                val card: View = binding.cardMoves
                if (!card.isAttachedToWindow) return@addUpdateListener
                val scale = value.animatedValue as Float
                card.scaleX = scale
                card.scaleY = scale
            } catch (e: Exception) {
                return@addUpdateListener
            }
        }
        animator.start()
        lowMoves = animator
    }

    fun showBanner(text: String?) {
        val banner = binding.textGameBanner
        if (text.isNullOrEmpty()) {
            banner.visibility = View.GONE
            return
        }
        banner.text = text
        if (banner.visibility == View.VISIBLE) return
        banner.alpha = 0f
        banner.visibility = View.VISIBLE
        try {
            banner.animate().alpha(1f).setDuration(BANNER_MS).start()
        } catch (e: Exception) {
            banner.alpha = 1f
        }
    }

    fun cancel() {
        stopLowMoves()
        try {
            countUp?.cancel()
        } catch (e: Exception) {
            countUp = null
        }
        countUp = null
        onScoreTick = null
        try {
            binding.textGameBanner.animate().cancel()
        } catch (e: Exception) {
            return
        }
    }

    private fun stopLowMoves() {
        try {
            lowMoves?.cancel()
        } catch (e: Exception) {
            lowMoves = null
        }
        lowMoves = null
        binding.cardMoves.scaleX = 1f
        binding.cardMoves.scaleY = 1f
    }

    private companion object {
        const val SCORE_MS = 460L
        const val LOW_MOVES_MS = 520L
        const val LOW_MOVES_CYCLES = 5
        const val BANNER_MS = 220L
    }
}
