package com.nazmar.musicgym

import androidx.fragment.app.Fragment

open class BaseFragment<BINDING_TYPE> : Fragment() {

    protected var _binding: BINDING_TYPE? = null
    protected val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}