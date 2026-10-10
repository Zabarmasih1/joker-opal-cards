package com.jokeropal.cards.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.jokeropal.cards.R
import com.jokeropal.cards.core.config.GameConfig
import com.jokeropal.cards.domain.model.BoardCell
import com.jokeropal.cards.domain.model.BoardSnapshot

class OpalBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    interface OnCellTapListener {
        fun onCellTap(row: Int, col: Int)
    }

    private val rows = GameConfig.BOARD_ROWS
    private val cols = GameConfig.BOARD_COLS
    private val boardFrame = ViewExt.dp(context, BOARD_PAD_DP) + ViewExt.dp(context, BOARD_BORDER_DP)
    private val cellGap = ViewExt.dp(context, GAP_DP)
    private val gemInset = ViewExt.dp(context, GEM_INSET_DP)
    private val slots = ArrayList<Slot>(rows * cols)

    private var tile = 0
    private var snapshot: BoardSnapshot? = null
    private var selectedIndex = -1
    private var highContrast = false
    private var animationsEnabled = true

    var tapListener: OnCellTapListener? = null

    init {
        clipChildren = false
        buildSlots()
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        animationsEnabled = enabled
    }

    fun render(board: BoardSnapshot, selected: Int, contrast: Boolean, animate: Boolean) {
        val previous = snapshot
        snapshot = board
        selectedIndex = selected
        highContrast = contrast
        for (index in slots.indices) {
            val slot = slots[index]
            val cell = board.cellAt(index)
            val changed = previous == null || !sameGem(previous.cellAt(index), cell)
            applyCell(slot, cell, index, changed && animate && animationsEnabled)
        }
    }

    fun flashCells(indices: Set<Int>) {
        if (!animationsEnabled) return
        for (index in indices) {
            if (index !in slots.indices) continue
            val slot = slots[index]
            try {
                slot.gem.animate().cancel()
                slot.gem.animate()
                    .alpha(0.25f)
                    .scaleX(0.6f)
                    .scaleY(0.6f)
                    .setDuration(GameConfig.CLEAR_ANIM_MS)
                    .start()
            } catch (e: Exception) {
                slot.gem.alpha = 1f
            }
        }
    }

    fun clearAnimations() {
        for (slot in slots) {
            ViewExt.cancelAnimation(slot.gem)
            ViewExt.cancelAnimation(slot.cage)
            slot.gem.alpha = 1f
            slot.gem.scaleX = 1f
            slot.gem.scaleY = 1f
            slot.gem.translationY = 0f
        }
    }

    override fun onDetachedFromWindow() {
        clearAnimations()
        super.onDetachedFromWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val screenWidth = resources.displayMetrics.widthPixels
        val gutter = ViewExt.dp(context, SCREEN_GUTTER_DP)
        val hardMax = ViewExt.dp(context, BOARD_MAX_DP)
        var limit = minOf(screenWidth - gutter, hardMax)
        val availableWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        if (availableWidth in 1 until limit) limit = availableWidth
        val availableHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        if (availableHeight in 1 until limit) limit = availableHeight

        val minTile = ViewExt.dp(context, MIN_TILE_DP)
        tile = ((limit - 2 * boardFrame) / cols).coerceAtLeast(minTile)
        val boardSize = tile * cols + 2 * boardFrame

        val childSize = (tile - cellGap).coerceAtLeast(1)
        val childSpec = View.MeasureSpec.makeMeasureSpec(childSize, View.MeasureSpec.EXACTLY)
        for (slot in slots) {
            slot.root.measure(childSpec, childSpec)
        }
        setMeasuredDimension(boardSize, boardSize)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val half = cellGap / 2
        for (index in slots.indices) {
            val slot = slots[index]
            val row = index / cols
            val col = index % cols
            val cellLeft = boardFrame + col * tile + half
            val cellTop = boardFrame + row * tile + half
            val size = (tile - cellGap).coerceAtLeast(1)
            slot.root.layout(cellLeft, cellTop, cellLeft + size, cellTop + size)
        }
    }

    private fun buildSlots() {
        for (index in 0 until rows * cols) {
            val root = FrameLayout(context)
            root.setBackgroundResource(R.drawable.cell_slot)
            root.isClickable = true
            root.isFocusable = true

            val gem = ImageView(context)
            gem.scaleType = ImageView.ScaleType.FIT_CENTER
            val gemParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
            gemParams.setMargins(gemInset, gemInset, gemInset, gemInset)
            gem.layoutParams = gemParams
            gem.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            root.addView(gem)

            val cage = ImageView(context)
            cage.scaleType = ImageView.ScaleType.FIT_CENTER
            cage.setImageResource(R.drawable.sprite_lock_cage)
            val cageParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
            cageParams.setMargins(gemInset, gemInset, gemInset, gemInset)
            cage.layoutParams = cageParams
            cage.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            cage.visibility = View.GONE
            root.addView(cage)

            val position = index
            root.setOnClickListener {
                try {
                    tapListener?.onCellTap(position / cols, position % cols)
                } catch (e: Exception) {
                    return@setOnClickListener
                }
            }

            addView(root)
            slots.add(Slot(root, gem, cage))
        }
    }

    private fun applyCell(slot: Slot, cell: BoardCell, index: Int, animate: Boolean) {
        if (cell.wild) {
            slot.gem.setImageResource(R.drawable.sprite_joker_card)
        } else {
            slot.gem.setImageResource(GemAssets.drawableFor(cell.gem))
        }
        slot.cage.visibility = if (cell.locked) View.VISIBLE else View.GONE

        val background = when {
            cell.locked -> R.drawable.cell_slot_locked
            index == selectedIndex -> R.drawable.cell_slot_selected
            highContrast -> R.drawable.cell_slot_contrast
            else -> R.drawable.cell_slot
        }
        slot.root.setBackgroundResource(background)

        val label = if (cell.locked) {
            context.getString(
                R.string.fmt_gem_caged_cd,
                cell.gem.displayName,
                cell.row + 1,
                cell.col + 1
            )
        } else {
            context.getString(
                R.string.fmt_gem_cd,
                cell.gem.displayName,
                cell.row + 1,
                cell.col + 1
            )
        }
        slot.root.contentDescription = label

        val lifted = index == selectedIndex && !cell.locked
        ViewExt.cancelAnimation(slot.gem)
        slot.gem.alpha = 1f
        if (animate) {
            slot.gem.translationY = -tile.toFloat()
            slot.gem.scaleX = 0.7f
            slot.gem.scaleY = 0.7f
            try {
                slot.gem.animate()
                    .translationY(0f)
                    .scaleX(if (lifted) SELECTED_SCALE else 1f)
                    .scaleY(if (lifted) SELECTED_SCALE else 1f)
                    .setDuration(GameConfig.FALL_ANIM_MS)
                    .start()
            } catch (e: Exception) {
                slot.gem.translationY = 0f
                slot.gem.scaleX = 1f
                slot.gem.scaleY = 1f
            }
        } else {
            slot.gem.translationY = 0f
            slot.gem.scaleX = if (lifted) SELECTED_SCALE else 1f
            slot.gem.scaleY = if (lifted) SELECTED_SCALE else 1f
        }
    }

    private fun sameGem(first: BoardCell, second: BoardCell): Boolean =
        first.gem == second.gem && first.wild == second.wild && first.locked == second.locked

    private class Slot(val root: FrameLayout, val gem: ImageView, val cage: ImageView)

    private companion object {
        const val BOARD_PAD_DP = 6f
        const val BOARD_BORDER_DP = 2f
        const val GAP_DP = 2f
        const val GEM_INSET_DP = 5f
        const val SCREEN_GUTTER_DP = 32f
        const val BOARD_MAX_DP = 380f
        const val MIN_TILE_DP = 22f
        const val SELECTED_SCALE = 1.08f
    }
}
