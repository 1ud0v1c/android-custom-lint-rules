package com.ludovic.vimont.customlintrules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ludovic.vimont.customlintrules.databinding.FragmentContentBinding
import com.ludovic.vimont.customlintrules.databinding.FragmentHelloWorldBinding

/**
 * There are an issue here.
 * We should clean the binding by overriding the onDestroyView() method.
 * A good candidate for a lint :D !
 */
class FragmentContent: Fragment() {
    private val fortyTwo: Int = 42

    private var _binding: FragmentContentBinding? = null
    private val binding get() = _binding!!

    private var _helloWorldBinding: FragmentHelloWorldBinding? = null
    private val helloWorldBinding get() = _helloWorldBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // We should set _helloWorldBinding to null here
    }
}