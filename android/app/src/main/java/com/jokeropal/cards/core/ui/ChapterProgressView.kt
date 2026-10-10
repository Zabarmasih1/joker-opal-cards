package com.jokeropal.cards.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.jokeropal.cards.R
import com.jokeropal.cards.databinding.ViewChapterProgressBinding

class ChapterProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewChapterProgressBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
    }

    fun bind(total: Int, completed: Int) {
        val pipSize = resources.getDimensionPixelSize(R.dimen.pip_size)
        val pipGap = resources.getDimensionPixelSize(R.dimen.pip_gap)
        binding.layoutPips.removeAllViews()
        for (position in 0 until total) {
            val pip = View(context)
            val params = LayoutParams(pipSize, pipSize)
            if (position > 0) params.marginStart = pipGap
            pip.layoutParams = params
            pip.setBackgroundResource(
                if (position < completed) R.drawable.pip_filled else R.drawable.pip_empty
            )
            pip.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            binding.layoutPips.addView(pip)
        }
        binding.textProgressCaption.text =
            context.getString(R.string.fmt_chapters_cleared, completed, total)
        ViewExt.describeState(
            this,
            context.getString(R.string.fmt_chapters_state, completed, total)
        )
    }
}
