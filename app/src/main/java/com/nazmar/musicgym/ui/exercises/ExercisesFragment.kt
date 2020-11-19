package com.nazmar.musicgym.ui.exercises

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nazmar.musicgym.R

class ExercisesFragment : Fragment() {

    private lateinit var exercisesViewModel: ExercisesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        exercisesViewModel =
            ViewModelProvider(this).get(ExercisesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_exercises, container, false)
        val textView: TextView = root.findViewById(R.id.exercises)
        exercisesViewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })
        return root
    }
}