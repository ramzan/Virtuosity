package ca.ramzan.virtuosity.screens.exercise_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.*
import ca.ramzan.virtuosity.databinding.FragmentExerciseListBinding
import ca.ramzan.virtuosity.exercises.Exercise
import ca.ramzan.virtuosity.screens.BaseFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class ExerciseListFragment : BaseFragment<FragmentExerciseListBinding>() {

    @Inject
    lateinit var imm: InputMethodManager

    private val viewModel: ExerciseListViewModel by viewModels()

    private lateinit var adapter: ExerciseAdapter

    override fun onStart() {
        super.onStart()
        if (requireArguments().getBoolean("editingRoutine")) {
            requireActivity().hideBottomNavBar()
        } else requireActivity().showBottomNavBar()
        setFragmentResultListener(TEXT_INPUT_RESULT) { _, bundle ->
            bundle.getString(INPUT_TEXT)?.let { viewModel.addExercise(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mutableBinding = FragmentExerciseListBinding.inflate(inflater)

        adapter = if (requireArguments().getBoolean("editingRoutine")) {
            setUpSelectionView()
            ExerciseAdapter { exercise, position ->
                viewModel.toggleSelected(exercise)
                adapter.notifyItemChanged(position)
            }
        } else ExerciseAdapter { exercise, _ -> showExerciseView(exercise.id) }

        binding.exerciseList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is ExerciseListState.Loaded -> {
                        adapter.submitSelected(viewModel.selectedExercises)
                        adapter.submitList(state.filteredExercises)
                        if (state.filteredExercises.isEmpty()) {
                            binding.exerciseList.visibility = View.GONE
                            binding.exerciseListProgressBar.visibility = View.GONE
                            binding.noExercisesMessage.visibility = View.VISIBLE
                        } else {
                            binding.exerciseList.visibility = View.VISIBLE
                            binding.exerciseListProgressBar.visibility = View.GONE
                            binding.noExercisesMessage.visibility = View.GONE
                        }
                    }
                    ExerciseListState.Loading -> {
                        binding.exerciseList.visibility = View.GONE
                        binding.exerciseListProgressBar.visibility = View.VISIBLE
                        binding.noExercisesMessage.visibility = View.GONE
                    }
                }
            }
        }

        binding.exercisesToolbar.menu.findItem(R.id.new_exercise).setOnMenuItemClickListener {
            showNewExerciseDialog()
            true
        }

        binding.exercisesToolbar.menu.findItem(R.id.search).apply {
            (actionView as SearchView).apply {
                maxWidth = Integer.MAX_VALUE
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

        if (requireArguments().getBoolean("exerciseDeleted")) {
            Snackbar.make(
                requireActivity().findViewById(R.id.nav_view),
                getString(R.string.exercise_deleted_message),
                Snackbar.LENGTH_SHORT
            )
                .setAnchorView(requireActivity().findViewById(R.id.nav_view))
                .show()
            requireArguments().remove("exerciseDeleted")
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

    // Adjust layout/behaviour for exercise selection in routine editor
    private fun setUpSelectionView() {
        binding.apply {
            root.setPadding(0, 0, 0, 0)
            exercisesToolbar.apply {
                setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
                setTitle(R.string.add_exercises)
                setNavigationOnClickListener {
                    requireActivity().onBackPressed()
                }
            }
            addExercisesFab.setOnClickListener {
                setFragmentResult(
                    ADD_EXERCISE_RESULTS,
                    bundleOf(
                        ADD_EXERCISE_RESULTS to viewModel.selectedExercises.toList()
                            .map { Exercise(it.name, it.id) })
                )
                requireActivity().onBackPressed()
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.numExercisesSelected.collect { numSelected ->
                    when {
                        numSelected > 0 -> {
                            addExercisesFab.text = resources.getQuantityString(
                                R.plurals.add_n_exercises,
                                numSelected,
                                numSelected
                            )
                            addExercisesFab.show()
                        }
                        numSelected == 0 -> addExercisesFab.hide()
                        else -> throw Exception("Illegal number of exercises selected: $numSelected")

                    }
                }
            }
        }
    }

    override fun onStop() {
        viewModel.setNameQuery("")
        imm.hideKeyboard(requireView().windowToken)
        super.onStop()
    }
}