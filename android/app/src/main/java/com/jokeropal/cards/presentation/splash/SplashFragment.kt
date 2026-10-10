package com.jokeropal.cards.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jokeropal.cards.R
import com.jokeropal.cards.core.di.AppViewModelFactory
import com.jokeropal.cards.core.navigation.Navigator
import com.jokeropal.cards.databinding.FragmentSplashBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private var animator: SplashAnimator? = null
    private var navigated = false

    private val viewModel: SplashViewModel by viewModels {
        AppViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSplashBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = _binding ?: return
        animator = SplashAnimator(binding).also { it.start() }
        observeState()
        viewModel.start()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.dots.collectLatest { renderCaption(it) }
                }
                launch {
                    viewModel.finished.collectLatest { done ->
                        if (done) leaveSplash()
                    }
                }
            }
        }
    }

    private fun renderCaption(dots: Int) {
        val binding = _binding ?: return
        val builder = StringBuilder(getString(R.string.splash_loading))
        for (step in 0 until dots) {
            builder.append('.')
        }
        binding.textSplashLoading.text = builder.toString()
    }

    private fun leaveSplash() {
        if (navigated) return
        if (!isAdded || view == null) return
        navigated = true
        viewModel.consumeFinished()
        try {
            Navigator.toMenu(parentFragmentManager)
        } catch (e: Exception) {
            navigated = false
        }
    }

    override fun onDestroyView() {
        animator?.cancel()
        animator = null
        _binding = null
        super.onDestroyView()
    }
}
