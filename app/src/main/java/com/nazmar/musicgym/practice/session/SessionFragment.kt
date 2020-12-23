package com.nazmar.musicgym.practice.session

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.nazmar.musicgym.databinding.FragmentSessionBinding
import com.nazmar.musicgym.hideBottomNavBar

class SessionFragment : Fragment() {

    private var _binding: FragmentSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionViewModel by viewModels {
        SessionViewModelFactory(
            arguments?.get(
                "routineId"
            ) as Long, requireNotNull(this.activity).application
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().hideBottomNavBar()

        _binding = FragmentSessionBinding.inflate(inflater)

        viewModel.exercises.observe(viewLifecycleOwner) {
            Log.d("zoop", it.size.toString())
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}