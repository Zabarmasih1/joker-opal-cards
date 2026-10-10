package com.jokeropal.cards.core.ui

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.jokeropal.cards.R
import com.jokeropal.cards.databinding.ViewJokerCardBinding
import com.jokeropal.cards.domain.model.JokerState
import com.jokeropal.cards.domain.model.JokerType

class JokerCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewJokerCardBinding.inflate(LayoutInflater.from(context), this)
    private var idlePulse: ValueAnimator? = null
    private var boundType: JokerType? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        minimumHeight = ViewExt.dp(context, 48f)
        val padding = ViewExt.dp(context, 6f)
        setPadding(padding, padding, padding, padding)
        setBackgroundResource(R.drawable.joker_slot_bg)
        isFocusable = true
    }

    fun bind(state: JokerState) {
        boundType = state.type
        binding.textJokerLabel.setText(GemAssets.labelFor(state.type))
        binding.progressJokerCharge.progress = state.percent
        binding.imageJokerFace.contentDescription = state.type.title
        if (state.isReady) {
            setBackgroundResource(R.drawable.joker_slot_bg_ready)
            alpha = 1f
            isEnabled = true
            elevation = ViewExt.dp(context, 10f).toFloat()
            ViewExt.describeState(
                this,
                context.getString(R.string.fmt_joker_ready, state.type.title)
            )
            startIdlePulse()
        } else {
            setBackgroundResource(R.drawable.joker_slot_bg)
            alpha = 0.45f
            isEnabled = false
            elevation = 0f
            ViewExt.describeState(
                this,
                context.getString(R.string.fmt_joker_locked, state.percent)
            )
            stopIdlePulse()
        }
    }

    fun type(): JokerType? = boundType

    fun playReveal() {
        try {
            animate().cancel()
            animate()
                .rotationY(90f)
                .setDuration(REVEAL_HALF_MS)
                .withEndAction {
                    try {
                        if (!isAttachedToWindow) return@withEndAction
                        rotationY = -90f
                        animate()
                            .rotationY(0f)
                            .setDuration(REVEAL_HALF_MS)
                            .start()
                    } catch (e: Exception) {
                        rotationY = 0f
                    }
                }
                .start()
        } catch (e: Exception) {
            rotationY = 0f
        }
    }

    private fun startIdlePulse() {
        if (idlePulse != null) return
        val animator = ValueAnimator.ofFloat(1f, 1.03f)
        animator.duration = PULSE_MS
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener { value ->
            try {
                if (!isAttachedToWindow) return@addUpdateListener
                val scale = value.animatedValue as Float
                scaleX = scale
                scaleY = scale
            } catch (e: Exception) {
                scaleX = 1f
                scaleY = 1f
            }
        }
        animator.start()
        idlePulse = animator
    }

    private fun stopIdlePulse() {
        try {
            idlePulse?.cancel()
        } catch (e: Exception) {
            idlePulse = null
        }
        idlePulse = null
        scaleX = 1f
        scaleY = 1f
    }

    override fun onDetachedFromWindow() {
        stopIdlePulse()
        ViewExt.cancelAnimation(this)
        super.onDetachedFromWindow()
    }

    private companion object {
        const val PULSE_MS = 1200L
        const val REVEAL_HALF_MS = 180L
    }
}
