package com.jokeropal.cards.presentation.splash

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.jokeropal.cards.databinding.FragmentSplashBinding

class SplashAnimator(private val binding: FragmentSplashBinding) {

    private val running = ArrayList<ValueAnimator>()

    fun start() {
        dealCards()
        raiseTitle()
        breatheOpal()
        sweepSheen()
    }

    private fun dealCards() {
        val cards = listOf(
            binding.imageCardLeft to 0L,
            binding.imageCardCenter to 120L,
            binding.imageCardRight to 240L
        )
        for (entry in cards) {
            val view = entry.first
            val delay = entry.second
            val restRotation = view.rotation
            val restTranslation = view.translationX
            view.alpha = 0f
            view.rotation = 0f
            view.translationX = 0f
            view.translationY = DEAL_DISTANCE
            try {
                view.animate()
                    .alpha(1f)
                    .rotation(restRotation)
                    .translationX(restTranslation)
                    .translationY(0f)
                    .setStartDelay(delay)
                    .setDuration(DEAL_MS)
                    .setInterpolator(OvershootInterpolator(1.1f))
                    .start()
            } catch (e: Exception) {
                restoreCard(view, restRotation, restTranslation)
            }
        }
        val opal = binding.imageCardOpal
        opal.alpha = 0f
        opal.scaleX = 0.6f
        opal.scaleY = 0.6f
        try {
            opal.animate()
                .alpha(0.95f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(360L)
                .setDuration(DEAL_MS)
                .setInterpolator(OvershootInterpolator(1.4f))
                .start()
        } catch (e: Exception) {
            opal.alpha = 0.95f
            opal.scaleX = 1f
            opal.scaleY = 1f
        }
    }

    private fun restoreCard(view: View, rotation: Float, translationX: Float) {
        view.alpha = 1f
        view.rotation = rotation
        view.translationX = translationX
        view.translationY = 0f
    }

    private fun raiseTitle() {
        val targets = listOf(binding.textSplashTitle, binding.textSplashSubtitle)
        var delay = TITLE_DELAY
        for (view in targets) {
            view.alpha = 0f
            view.translationY = TITLE_RISE
            try {
                view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(delay)
                    .setDuration(TITLE_MS)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            } catch (e: Exception) {
                view.alpha = 1f
                view.translationY = 0f
            }
            delay += 90L
        }
    }

    private fun breatheOpal() {
        val animator = ValueAnimator.ofFloat(1f, 1.06f)
        animator.duration = BREATHE_MS
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { value ->
            try {
                val target = binding.imageCardOpal
                if (!target.isAttachedToWindow) return@addUpdateListener
                val scale = value.animatedValue as Float
                target.scaleX = scale
                target.scaleY = scale
                binding.viewSplashGlow.alpha = GLOW_BASE + (scale - 1f) * GLOW_SWING
            } catch (e: Exception) {
                return@addUpdateListener
            }
        }
        animator.start()
        running.add(animator)
    }

    private fun sweepSheen() {
        val animator = ValueAnimator.ofFloat(-SHEEN_TRAVEL, SHEEN_TRAVEL)
        animator.duration = SHEEN_MS
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.addUpdateListener { value ->
            try {
                val target = binding.viewSplashSheen
                if (!target.isAttachedToWindow) return@addUpdateListener
                target.translationX = value.animatedValue as Float
            } catch (e: Exception) {
                return@addUpdateListener
            }
        }
        animator.start()
        running.add(animator)
    }

    fun cancel() {
        for (animator in running) {
            try {
                animator.cancel()
            } catch (e: Exception) {
                continue
            }
        }
        running.clear()
        val views = listOf<View>(
            binding.imageCardLeft,
            binding.imageCardCenter,
            binding.imageCardRight,
            binding.imageCardOpal,
            binding.textSplashTitle,
            binding.textSplashSubtitle,
            binding.viewSplashSheen
        )
        for (view in views) {
            try {
                view.animate().cancel()
            } catch (e: Exception) {
                continue
            }
        }
    }

    private companion object {
        const val DEAL_MS = 520L
        const val DEAL_DISTANCE = 160f
        const val TITLE_MS = 420L
        const val TITLE_DELAY = 560L
        const val TITLE_RISE = 28f
        const val BREATHE_MS = 1400L
        const val SHEEN_MS = 2600L
        const val SHEEN_TRAVEL = 220f
        const val GLOW_BASE = 0.75f
        const val GLOW_SWING = 4f
    }
}
