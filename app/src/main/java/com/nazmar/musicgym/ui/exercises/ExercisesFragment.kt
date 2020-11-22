package com.nazmar.musicgym.ui.exercises

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
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
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_exercises, container, false
        )

        val adapter = ExerciseAdapter(ExerciseAdapter.OnClickListener {
            Toast.makeText(requireContext(), it.name, Toast.LENGTH_SHORT).show()
        })

        binding.exerciseList.adapter = adapter

        viewModel.exercises.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        binding.fab.setOnClickListener {
            it?.apply { isEnabled = false; postDelayed({ isEnabled = true }, 400) } //400 ms
//            showEditDialog(null)
            viewModel.addExercise()
        }

        binding.exercisesToolbar.menu.findItem(R.id.search).apply {
            (actionView as SearchView).apply {
                isIconified = false
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(text: String?): Boolean {
                        return true
                    }

                    override fun onQueryTextChange(text: String?): Boolean {
                        viewModel.setNameQuery(text.toString())
                        return true
                    }
                })
            }

            val imm = (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                    imm.toggleSoftInputFromWindow(requireView().windowToken, InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS)
                    return true
                }

                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                    viewModel.setNameQuery("")
                    imm.hideSoftInputFromWindow(requireView().windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                    return true
                }

            })
        }

        binding.exercisesToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sort -> {
                    true
                }
                R.id.search -> {
                    true
                }
                else -> false
            }
        }

        return binding.root
    }

    override fun onStop() {
        viewModel.setNameQuery("")
        super.onStop()
    }
}