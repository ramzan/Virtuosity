package com.nazmar.musicgym.screens.exerciselist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.R
import com.nazmar.musicgym.common.*
import com.nazmar.musicgym.databinding.FragmentExerciseListBinding
import com.nazmar.musicgym.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class ExerciseListFragment : BaseFragment<FragmentExerciseListBinding>() {

    @Inject
    lateinit var imm: InputMethodManager

    private val viewModel: ExerciseListViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        requireActivity().showBottomNavBar()
        setFragmentResultListener(TEXT_INPUT_RESULT) { _, bundle ->
            bundle.getString(INPUT_TEXT)?.let { viewModel.addExercise(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseListBinding.inflate(inflater)

        val adapter = ExerciseAdapter { exercise -> showExerciseView(exercise.id) }

        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.exerciseList.adapter = adapter

        lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is ExerciseListState.Loaded -> {
                        binding.exerciseList.visibility = View.VISIBLE
                        binding.exerciseListProgressBar.visibility = View.GONE
                        adapter.submitList(state.filteredExercises)
                    }
                    ExerciseListState.Loading -> {
                        binding.exerciseList.visibility = View.GONE
                        binding.exerciseListProgressBar.visibility = View.VISIBLE
                    }
                }
            }
        }


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

            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                    (actionView as SearchView).onActionViewExpanded()
                    imm.showKeyboard()
                    (actionView as SearchView).requestFocus()
                    return true
                }

                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                    viewModel.setNameQuery("")
                    (actionView as SearchView).setQuery("", false)
                    imm.hideKeyboard(requireView().windowToken)
                    return true
                }
            })
        }
        return binding.root
    }

    override fun onDestroyView() {
        binding.exerciseList.adapter = null
        super.onDestroyView()
    }

    private fun showExerciseView(id: Long) {
        findNavController().safeNavigate(
            ExerciseListFragmentDirections.actionExercisesFragmentToExercisesGraph(id)
        )
    }

    private fun showNewExerciseDialog() {
        findNavController().safeNavigate(
            ExerciseListFragmentDirections.actionExerciseListFragmentToTextInputDialog(
                R.string.new_exercise,
                ""
            )
        )
    }

    override fun onStop() {
        viewModel.setNameQuery("")
        imm.hideKeyboard(requireView().windowToken)
        super.onStop()
    }
}