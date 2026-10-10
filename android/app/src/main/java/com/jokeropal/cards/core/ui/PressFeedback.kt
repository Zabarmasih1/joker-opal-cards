package com.jokeropal.cards.core.ui

import android.view.View
import android.view.animation.OvershootInterpolator

object PressFeedback {

    private const val DOWN_MS = 110L
    private const val UP_MS = 180L
    private const val PRESSED_SCALE = 0.96f

    fun pulse(view: View) {
        try {
            view.animate().cancel()
            view.animate()
                .scaleX(PRESSED_SCALE)
                .scaleY(PRESSED_SCALE)
                .setDuration(DOWN_MS)
                .withEndAction {
                    try {
                        if (!view.isAttachedToWindow) return@withEndAction
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(UP_MS)
                            .setInterpolator(OvershootInterpolator(2f))
                            .start()
                    } catch (e: Exception) {
                        view.scaleX = 1f
                        view.scaleY = 1f
                    }
                }
                .start()
        } catch (e: Exception) {
            view.scaleX = 1f
            view.scaleY = 1f
        }
    }

    fun release(view: View?) {
        try {
            view?.animate()?.cancel()
            view?.scaleX = 1f
            view?.scaleY = 1f
        } catch (e: Exception) {
            return
        }
    }
}
