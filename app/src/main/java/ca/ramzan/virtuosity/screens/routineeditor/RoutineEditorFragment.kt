package ca.ramzan.virtuosity.screens.routineeditor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.RecyclerView
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.*
import ca.ramzan.virtuosity.databinding.FragmentRoutineEditorBinding
import ca.ramzan.virtuosity.exercises.Exercise
import ca.ramzan.virtuosity.screens.BaseFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class RoutineEditorFragment : BaseFragment<FragmentRoutineEditorBinding>() {

    @Inject
    lateinit var imm: InputMethodManager

    @Inject
    lateinit var factory: RoutineEditorViewModel.Factory

    private val viewModel: RoutineEditorViewModel by viewModels {
        RoutineEditorViewModel.provideFactory(factory, requireArguments().getLong("routineId"))
    }

    private val adapter = RoutineExerciseAdapter(::showDurationPicker, ::deleteItem)

    private val simpleItemTouchCallback =
        object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if (viewHolder.itemViewType != target.itemViewType) return false

                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition
                viewModel.moveItem(fromPos, toPos)
                adapter.notifyItemMoved(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteItem(viewHolder.bindingAdapterPosition)
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState != ACTION_STATE_IDLE) viewHolder?.itemView?.alpha = 0.5f
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
            }
        }

    private val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)

    override fun onStart() {
        super.onStart()
        setFragmentResultListener(CONFIRMATION_RESULT) { _, bundle ->
            if (bundle.getBoolean(POSITIVE_RESULT)) {
                viewModel.deleteRoutine()
                goBack()
            }
        }
        setFragmentResultListener(DURATION_PICKER_RESULT) { _, bundle ->
            viewModel.updateDuration(bundle.getLong(DURATION_VALUE))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().hideBottomNavBar()

        val firstRun = savedInstanceState?.getBoolean(FIRST_RUN_KEY) ?: true

        _binding = FragmentRoutineEditorBinding.inflate(inflater)

        val deleteButton = binding.editorToolbar.menu.getItem(0)
        val saveButton = binding.editorToolbar.menu.getItem(1)

        binding.apply {
            editorToolbar.setNavigationOnClickListener {
                goBack()
            }

            saveButton.setOnMenuItemClickListener {
                if (nameInput.text?.trim().isNullOrEmpty()) {
                    nameInput.setText("")
                    nameInputLayout.isErrorEnabled = true
                    nameInputLayout.error = getString(R.string.empty_name_error_msg)
                } else {
                    viewModel.saveRoutine()
                    goBack()
                }
                true
            }
            deleteButton.setOnMenuItemClickListener {
                showDeleteDialog()
                true
            }

            nameInput.doOnTextChanged { text, _, _, _ ->
                viewModel.state.value.nameInputText = text.toString().trim().replace('\n', ' ')
            }

            exerciseSpinner.apply {
                onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
                    viewModel.addExercise(this.adapter.getItem(pos) as Exercise)
                    binding.routineExerciseList.adapter?.notifyItemInserted(viewModel.state.value.exercises.size)
                    setText("")
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.allExercises.collect { list ->
                    exerciseSpinner.setAdapter(
                        ArrayAdapter(requireContext(), R.layout.list_item_exercise_spinner, list)
                    )
                }
            }
        }

        binding.routineExerciseList.adapter = adapter
        itemTouchHelper.attachToRecyclerView(binding.routineExerciseList)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                adapter.submitList(state.exercises)
                when (state) {
                    RoutineEditorState.Loading -> {
                        /* no-op */
                    }
                    RoutineEditorState.Deleted -> goBack()
                    is RoutineEditorState.Editing -> {
                        binding.apply {
                            editorToolbar.title = getString(R.string.editorTitleEdit)
                            if (firstRun) nameInput.setText(state.nameInputText)

                            nameInput.setSelection(state.nameInputText.length)
                            deleteButton.isVisible = true
                            saveButton.isVisible = true
                        }
                    }
                    is RoutineEditorState.New -> {
                        binding.editorToolbar.title = getString(R.string.editorTitleNew)
                        saveButton.isVisible = true
                        if (firstRun) {
                            binding.nameInput.requestFocus()
                            imm.showKeyboard()
                        }
                    }
                }
            }
        }
        return binding.root
    }

    private fun showDurationPicker(exerciseIndex: Int, duration: Long) {
        viewModel.indexToUpdate = exerciseIndex
        findNavController().safeNavigate(
            RoutineEditorFragmentDirections.actionRoutineEditorFragmentToDurationPickerDialogFragment(
                duration
            )
        )
    }

    private fun deleteItem(position: Int) {
        val deleted = viewModel.deleteItem(position)
        adapter.notifyItemRemoved(position)
        Snackbar.make(
            binding.root,
            getString(R.string.routine_editor_exercise_removed_message),
            Snackbar.LENGTH_SHORT
        )
            .setAction(getString(R.string.undo)) {
                viewModel.undoDelete(deleted, position)
                adapter.notifyItemInserted(position)
            }
            .show()
    }

    private fun showDeleteDialog() {
        findNavController().safeNavigate(
            RoutineEditorFragmentDirections.actionRoutineEditorFragmentToConfirmationDialog(
                R.string.delete_routine_dialog_message
            )
        )
    }

    private fun goBack() {
        imm.hideKeyboard(requireView().windowToken)
        findNavController().popBackStack(R.id.routineListFragment, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FIRST_RUN_KEY, false)
    }
}