package ca.ramzan.virtuosity.screens.routine_editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
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

    private val adapter =
        RoutineExerciseAdapter(::showDurationPicker, ::deleteItem, ::showExercisePicker)

    private val simpleItemTouchCallback =
        object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {

            private var dragFrom = -1
            private var dragTo = -1

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags =
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END
                val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                return if (viewHolder.itemViewType == ITEM_VIEW_TYPE_ADD_BUTTON) 0 else makeMovementFlags(
                    dragFlags,
                    swipeFlags
                )
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if (viewHolder.itemViewType != target.itemViewType) return false

                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition

                if (dragFrom == -1) dragFrom = fromPos
                dragTo = toPos

                adapter.currentList.toMutableList().run {
                    this.add(toPos, this.removeAt(fromPos))
                    adapter.submitList(this)
                }
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteItem(viewHolder.bindingAdapterPosition)
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState != ACTION_STATE_IDLE) viewHolder?.itemView?.alpha = 0.5f
                if (dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                    viewModel.moveItem(dragFrom, dragTo)
                }
                dragFrom = -1
                dragTo = -1
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = warnDiscardChanges()
            })
    }

    override fun onStart() {
        super.onStart()
        setFragmentResultListener(CONFIRMATION_RESULT) { _, bundle ->
            if (bundle.getBoolean(DELETE_ROUTINE)) {
                viewModel.deleteRoutine()
                goBack(deleted = true)
            } else if (bundle.getBoolean(DISCARD_CHANGES)) {
                goBack()
            }
        }
        setFragmentResultListener(DURATION_PICKER_RESULT) { _, bundle ->
            viewModel.updateDuration(bundle.getLong(DURATION_VALUE))
        }
        setFragmentResultListener(ADD_EXERCISE_RESULTS) { _, bundle ->
            @Suppress("UNCHECKED_CAST")
            (bundle.get(ADD_EXERCISE_RESULTS) as? List<Exercise>)?.let { viewModel.addExercises(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().hideBottomNavBar()

        setUpBinding(FragmentRoutineEditorBinding.inflate(inflater))

        val deleteButton = binding.editorToolbar.menu.getItem(0)
        val saveButton = binding.editorToolbar.menu.getItem(1)

        binding.apply {
            editorToolbar.setNavigationOnClickListener {
                warnDiscardChanges()
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
        }

        binding.routineExerciseList.adapter = adapter
        itemTouchHelper.attachToRecyclerView(binding.routineExerciseList)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                adapter.submitWithFooter(state.exercises)
                when (state) {
                    RoutineEditorState.Loading -> {
                        /* no-op */
                    }
                    RoutineEditorState.Deleted -> goBack(deleted = true)
                    is RoutineEditorState.Editing -> {
                        binding.apply {
                            editorToolbar.title = getString(R.string.editorTitleEdit)
                            if (viewModel.firstRun) nameInput.setText(state.nameInputText)
                            nameInput.setSelection(state.nameInputText.length)
                            deleteButton.isVisible = true
                            saveButton.isVisible = true
                            viewModel.firstRun = false
                        }
                    }
                    is RoutineEditorState.New -> {
                        binding.editorToolbar.title = getString(R.string.editorTitleNew)
                        saveButton.isVisible = true
                        if (viewModel.firstRun) {
                            binding.nameInput.requestFocus()
                            imm.showKeyboard()
                            viewModel.firstRun = false
                        }
                    }
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        itemTouchHelper.attachToRecyclerView(null)
        binding.routineExerciseList.adapter = null
        super.onDestroyView()
    }

    private fun warnDiscardChanges() {
        findNavController().safeNavigate(
            RoutineEditorFragmentDirections.actionRoutineEditorFragmentToConfirmationDialog(
                R.string.discard_changes_dialog_title,
                R.string.discard_changes_dialog_message,
                R.string.discard,
                DISCARD_CHANGES
            )
        )
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
        Snackbar.make(
            binding.root,
            getString(R.string.routine_editor_exercise_removed_message),
            Snackbar.LENGTH_SHORT
        )
            .setAction(getString(R.string.undo)) {
                viewModel.undoDelete(deleted, position)
            }
            .show()
    }

    private fun showExercisePicker() {
        findNavController().safeNavigate(
            RoutineEditorFragmentDirections.actionRoutineEditorFragmentToExerciseListFragment()
                .apply { editingRoutine = true })
    }

    private fun showDeleteDialog() {
        findNavController().safeNavigate(
            RoutineEditorFragmentDirections.actionRoutineEditorFragmentToConfirmationDialog(
                R.string.delete_routine_dialog_title,
                R.string.message_action_cannot_be_undone,
                R.string.delete,
                DELETE_ROUTINE
            )
        )
    }

    private fun goBack(deleted: Boolean = false) {
        imm.hideKeyboard(requireView().windowToken)
        findNavController().popBackStack(R.id.routineEditorFragment, false)
        findNavController().safeNavigate(
            RoutineEditorFragmentDirections.actionRoutineEditorFragmentToRoutineListFragment()
                .apply {
                    routineDeleted = deleted
                }
        )
    }
}