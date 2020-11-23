package com.nazmar.musicgym.ui.exercises

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nazmar.musicgym.R
import com.nazmar.musicgym.databinding.FragmentExercisesBinding
import com.nazmar.musicgym.db.Exercise


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
    ): View {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_exercises, container, false
        )

        val adapter = ExerciseAdapter(ExerciseAdapter.OnClickListener {
            showExerciseView(it)
        })

        binding.exerciseList.adapter = adapter

        viewModel.exercises.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        binding.fab.setOnClickListener {
            showNewExerciseDialog()
        }

        binding.exercisesToolbar.menu.findItem(R.id.search).apply {
            (actionView as SearchView).apply {
                isIconified = false
                queryHint = getString(R.string.exercises_search_hint)
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
                    (actionView as SearchView).onActionViewExpanded()
                    imm.toggleSoftInputFromWindow(requireView().windowToken, InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS)
                    (actionView as SearchView).requestFocus()
                    return true
                }

                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                    viewModel.setNameQuery("")
                    (actionView as SearchView).setQuery("", false)
                    imm.hideSoftInputFromWindow(requireView().windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                    return true
                }

            })
        }
        return binding.root
    }

    private fun showExerciseView(exercise: Exercise) {
        val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        val action = ExercisesFragmentDirections.actionExercisesFragmentToExerciseViewFragment(exercise.id)
        navController.navigate(action)
    }

    private fun showNewExerciseDialog(): Boolean {
        val layout = layoutInflater.inflate(R.layout.text_input_dialog, null)
        val text = layout.findViewById<EditText>(R.id.name_input)

        val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.new_exercise)
                .setView(layout)
                .setPositiveButton("OK") { _, _ ->
                    viewModel.addExercise(text.text.toString().trim())
                }
                .setNegativeButton("CANCEL") { _, _ -> }
                .create()

        text.addTextChangedListener(object : TextWatcher {
            private fun handleText() {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                okButton.isEnabled = text.text.isNotEmpty()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                handleText()
            }
        })
        dialog.show()
        return true
    }

    override fun onStop() {
        viewModel.setNameQuery("")
        super.onStop()
    }
}