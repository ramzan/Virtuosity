package com.nazmar.musicgym.ui.exercises

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.nazmar.musicgym.R
import com.nazmar.musicgym.databinding.FragmentExercisesBinding

class ExercisesFragment : Fragment() {

    private lateinit var binding: FragmentExercisesBinding
    private val viewModel: ExercisesViewModel by activityViewModels {
        ExercisesViewModelFactory(
            requireNotNull(this.activity).application
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate view and get instance of binding class
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_exercises, container, false
        )

        // Set the recyclerview adapter
        val adapter = ExerciseAdapter(ExerciseAdapter.OnClickListener {
//            showEditDialog(it)
        })

        binding.liftList.adapter = adapter

        viewModel.exercises.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        binding.fab.setOnClickListener {
//            it?.apply { isEnabled = false; postDelayed({ isEnabled = true }, 400) } //400 ms
//            showEditDialog(null)
            viewModel.addExercise()
        }


        return binding.root
    }
}