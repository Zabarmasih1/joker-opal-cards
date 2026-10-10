package com.jokeropal.cards.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jokeropal.cards.R
import com.jokeropal.cards.core.di.ServiceLocator
import com.jokeropal.cards.core.ui.PressFeedback
import com.jokeropal.cards.core.ui.ViewExt
import com.jokeropal.cards.databinding.DialogSettingsBinding
import com.jokeropal.cards.domain.model.AppSettings

class SettingsDialog : DialogFragment() {

    private var _binding: DialogSettingsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_JokerOpalCards_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialogSettingsBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = _binding ?: return
        val current = readSettings()

        binding.switchSound.isChecked = current.soundEnabled
        binding.switchAnimations.isChecked = current.animationsEnabled
        binding.switchContrast.isChecked = current.highContrastGems
        describe(binding.switchSound, current.soundEnabled)
        describe(binding.switchAnimations, current.animationsEnabled)
        describe(binding.switchContrast, current.highContrastGems)

        binding.switchSound.setOnCheckedChangeListener { target, checked ->
            describe(target, checked)
            persist()
        }
        binding.switchAnimations.setOnCheckedChangeListener { target, checked ->
            describe(target, checked)
            persist()
        }
        binding.switchContrast.setOnCheckedChangeListener { target, checked ->
            describe(target, checked)
            persist()
        }

        binding.buttonSettingsClose.setOnClickListener { target ->
            PressFeedback.pulse(target)
            dismissAllowingStateLoss()
        }
        binding.buttonResetProgress.setOnClickListener { target ->
            PressFeedback.pulse(target)
            confirmReset()
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        } catch (e: Exception) {
            return
        }
    }

    private fun describe(target: View, checked: Boolean) {
        ViewExt.describeState(
            target,
            getString(if (checked) R.string.state_on else R.string.state_off)
        )
    }

    private fun readSettings(): AppSettings {
        val binding = _binding
        val context = binding?.root?.context ?: return AppSettings(
            soundEnabled = true,
            animationsEnabled = true,
            highContrastGems = false
        )
        return try {
            ServiceLocator.settings(context).settings()
        } catch (e: Exception) {
            AppSettings(soundEnabled = true, animationsEnabled = true, highContrastGems = false)
        }
    }

    private fun persist() {
        val binding = _binding ?: return
        try {
            ServiceLocator.settings(binding.root.context).update(
                AppSettings(
                    soundEnabled = binding.switchSound.isChecked,
                    animationsEnabled = binding.switchAnimations.isChecked,
                    highContrastGems = binding.switchContrast.isChecked
                )
            )
        } catch (e: Exception) {
            return
        }
    }

    private fun confirmReset() {
        if (!isAdded) return
        val context = _binding?.root?.context ?: return
        try {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.settings_reset_title)
                .setMessage(R.string.settings_reset_body)
                .setNegativeButton(R.string.settings_reset_cancel) { confirm, position ->
                    confirm.dismiss()
                }
                .setPositiveButton(R.string.settings_reset_confirm) { confirm, position ->
                    applyReset()
                    confirm.dismiss()
                    dismissAllowingStateLoss()
                }
                .show()
        } catch (e: Exception) {
            return
        }
    }

    private fun applyReset() {
        val binding = _binding ?: return
        try {
            ServiceLocator.progress(binding.root.context).reset()
        } catch (e: Exception) {
            return
        }
    }

    override fun onDestroyView() {
        val binding = _binding
        if (binding != null) {
            binding.switchSound.setOnCheckedChangeListener(null)
            binding.switchAnimations.setOnCheckedChangeListener(null)
            binding.switchContrast.setOnCheckedChangeListener(null)
            PressFeedback.release(binding.buttonSettingsClose)
            PressFeedback.release(binding.buttonResetProgress)
        }
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "settings_dialog"
    }
}
