package com.nazmar.musicgym.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nazmar.musicgym.R

class PracticeFragment : Fragment() {

    private lateinit var practiceViewModel: PracticeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        practiceViewModel =
                ViewModelProvider(this).get(PracticeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_practice, container, false)
        val textView: TextView = root.findViewById(R.id.practice)
        practiceViewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })
        return root
    }
}