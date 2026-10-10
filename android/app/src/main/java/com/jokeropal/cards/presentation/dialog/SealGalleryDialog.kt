package com.jokeropal.cards.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.jokeropal.cards.R
import com.jokeropal.cards.core.di.ServiceLocator
import com.jokeropal.cards.core.ui.PressFeedback
import com.jokeropal.cards.core.ui.ViewExt
import com.jokeropal.cards.databinding.DialogSealGalleryBinding
import com.jokeropal.cards.domain.model.SealRecord

class SealGalleryDialog : DialogFragment() {

    private var _binding: DialogSealGalleryBinding? = null
    private val adapter = SealAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_JokerOpalCards_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialogSealGalleryBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = _binding ?: return
        binding.recyclerSeals.layoutManager = GridLayoutManager(binding.root.context, SPAN_COUNT)
        binding.recyclerSeals.adapter = adapter
        binding.buttonSealsClose.setOnClickListener { target ->
            PressFeedback.pulse(target)
            dismissAllowingStateLoss()
        }
        binding.buttonSealsRetry.setOnClickListener { target ->
            PressFeedback.pulse(target)
            loadSeals()
        }
        loadSeals()
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

    private fun loadSeals() {
        val binding = _binding ?: return
        val records: List<SealRecord>? = try {
            ServiceLocator.progress(binding.root.context).seals()
        } catch (e: Exception) {
            null
        }
        if (records == null) {
            ViewExt.show(binding.recyclerSeals, false)
            ViewExt.show(binding.layoutSealsEmpty, false)
            ViewExt.show(binding.layoutSealsError, true)
            binding.textSealsCount.text = getString(R.string.seals_error)
            return
        }
        ViewExt.show(binding.layoutSealsError, false)
        val earned = records.count { it.earned }
        binding.textSealsCount.text =
            getString(R.string.fmt_seals_count, earned, records.size)
        if (earned == 0) {
            ViewExt.show(binding.recyclerSeals, false)
            ViewExt.show(binding.layoutSealsEmpty, true)
            return
        }
        ViewExt.show(binding.layoutSealsEmpty, false)
        ViewExt.show(binding.recyclerSeals, true)
        adapter.submit(records)
    }

    override fun onDestroyView() {
        val binding = _binding
        if (binding != null) {
            binding.recyclerSeals.adapter = null
            PressFeedback.release(binding.buttonSealsClose)
            PressFeedback.release(binding.buttonSealsRetry)
        }
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "seal_gallery_dialog"
        private const val SPAN_COUNT = 3
    }
}
