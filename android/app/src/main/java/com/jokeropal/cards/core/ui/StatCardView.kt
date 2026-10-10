package com.jokeropal.cards.core.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.jokeropal.cards.R
import com.jokeropal.cards.databinding.ViewStatCardBinding

class StatCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewStatCardBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        background = ContextCompat.getDrawable(context, R.drawable.stat_card_bg)
        val horizontal = ViewExt.dp(context, 8f)
        val vertical = ViewExt.dp(context, 12f)
        setPadding(horizontal, vertical, horizontal, vertical)
    }

    fun bind(iconRes: Int, value: String, label: String, valueColor: Int) {
        binding.imageStatIcon.setImageResource(iconRes)
        binding.imageStatIcon.imageTintList = ColorStateList.valueOf(valueColor)
        binding.layoutStatIcon.backgroundTintList =
            ColorStateList.valueOf(ColorUtils.setAlphaComponent(valueColor, TINT_ALPHA))
        binding.textStatValue.text = value
        binding.textStatValue.setTextColor(valueColor)
        binding.textStatLabel.text = label
        contentDescription = label + " " + value
    }

    fun bindCompactIcon(visible: Boolean) {
        ViewExt.show(binding.layoutStatIcon, visible)
    }

    private companion object {
        const val TINT_ALPHA = 38
    }
}
