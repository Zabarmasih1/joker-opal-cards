package com.jokeropal.cards.presentation.menu

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
import com.jokeropal.cards.core.ui.PressFeedback
import com.jokeropal.cards.core.ui.ViewExt
import com.jokeropal.cards.databinding.FragmentMenuBinding
import com.jokeropal.cards.presentation.dialog.SealGalleryDialog
import com.jokeropal.cards.presentation.dialog.SettingsDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null

    private val viewModel: MenuViewModel by viewModels {
        AppViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMenuBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = _binding ?: return

        binding.buttonPlay.setOnClickListener { target ->
            PressFeedback.pulse(target)
            openGame()
        }
        binding.buttonSeals.setOnClickListener { target ->
            PressFeedback.pulse(target)
            showSeals()
        }
        binding.buttonSettings.setOnClickListener { target ->
            PressFeedback.pulse(target)
            showSettings()
        }

        playEntrance(binding)
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { render(it) }
            }
        }
    }

    private fun render(state: MenuUiState) {
        val binding = _binding ?: return
        val context = binding.root.context

        binding.textMenuChips.text = getString(R.string.fmt_score, state.chips)
        binding.cardChipsBadge.contentDescription =
            getString(R.string.menu_chips_label) + " " + state.chips

        binding.textMenuChapter.text = getString(
            R.string.fmt_chapter_line,
            state.chapterIndex,
            state.chapterTitle
        )

        binding.viewChapterProgress.bind(state.totalChapters, state.chaptersCleared)

        ViewExt.show(binding.layoutMenuStats, state.statsVisible)
        if (state.statsVisible) {
            binding.statMenuCascade.bind(
                R.drawable.ic_cascade,
                getString(R.string.fmt_cascade, state.bestCascade),
                getString(R.string.label_best_cascade),
                ContextCompat.getColor(context, R.color.accent_gold)
            )
            binding.statMenuSeals.bind(
                R.drawable.ic_seal,
                state.seals.toString(),
                getString(R.string.label_joker_seals),
                ContextCompat.getColor(context, R.color.secondary_crimson)
            )
        }
    }

    private fun playEntrance(binding: FragmentMenuBinding) {
        ViewExt.fadeIn(binding.layoutMenuSheet, 0L, SHEET_RISE)
        ViewExt.fadeIn(binding.statMenuCascade, 160L, STAT_RISE)
        ViewExt.fadeIn(binding.statMenuSeals, 240L, STAT_RISE)
        binding.imageMenuOpal.animate()
            .scaleX(1.04f)
            .scaleY(1.04f)
            .setDuration(1800L)
            .start()
    }

    private fun openGame() {
        if (!isAdded || view == null) return
        try {
            Navigator.toGame(parentFragmentManager)
        } catch (e: Exception) {
            return
        }
    }

    private fun showSeals() {
        if (!isAdded) return
        try {
            SealGalleryDialog().show(parentFragmentManager, SealGalleryDialog.TAG)
        } catch (e: Exception) {
            return
        }
    }

    private fun showSettings() {
        if (!isAdded) return
        try {
            SettingsDialog().show(parentFragmentManager, SettingsDialog.TAG)
        } catch (e: Exception) {
            return
        }
    }

    override fun onDestroyView() {
        val binding = _binding
        if (binding != null) {
            PressFeedback.release(binding.buttonPlay)
            PressFeedback.release(binding.buttonSeals)
            PressFeedback.release(binding.buttonSettings)
            ViewExt.cancelAnimation(binding.layoutMenuSheet)
            ViewExt.cancelAnimation(binding.statMenuCascade)
            ViewExt.cancelAnimation(binding.statMenuSeals)
            ViewExt.cancelAnimation(binding.imageMenuOpal)
        }
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val SHEET_RISE = 60f
        const val STAT_RISE = 18f
    }
}
