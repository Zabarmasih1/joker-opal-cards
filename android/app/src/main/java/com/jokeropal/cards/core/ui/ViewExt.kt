package com.jokeropal.cards.core.ui

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.core.view.ViewCompat

object ViewExt {

    fun dp(context: Context, value: Float): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value,
        context.resources.displayMetrics
    ).toInt()

    fun describeState(view: View, state: CharSequence) {
        try {
            ViewCompat.setStateDescription(view, state)
        } catch (e: Exception) {
            view.contentDescription = state
        }
    }

    fun show(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun fadeIn(view: View, delay: Long, distance: Float) {
        view.alpha = 0f
        view.translationY = distance
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(delay)
            .setDuration(360L)
            .start()
    }

    fun cancelAnimation(view: View?) {
        try {
            view?.animate()?.cancel()
        } catch (e: Exception) {
            return
        }
    }
}
