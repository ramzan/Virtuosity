package com.nazmar.musicgym.screens.routineeditor

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
import com.nazmar.musicgym.*
import com.nazmar.musicgym.common.*
import com.nazmar.musicgym.databinding.FragmentRoutineEditorBinding
import com.nazmar.musicgym.exercises.Exercise
import com.nazmar.musicgym.screens.BaseFragment
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

    private val simpleItemTouchCallback =
        object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            val adapter =
                RoutineExerciseAdapter(this@RoutineEditorFragment, ::showDurationPicker).also {
                    it.stateRestorationPolicy =
                        RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                }

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
                viewModel.deleteItem(viewHolder.bindingAdapterPosition)
                adapter.notifyItemRemoved(viewHolder.bindingAdapterPosition)
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

            // Populate exercise autocomplete
            lifecycleScope.launchWhenStarted {
                viewModel.allExercises.collect { list ->
                    exerciseSpinner.setAdapter(
                        ArrayAdapter(requireContext(), R.layout.list_item_exercise_spinner, list)
                    )
                }
            }
        }

        binding.routineExerciseList.adapter = simpleItemTouchCallback.adapter
        itemTouchHelper.attachToRecyclerView(binding.routineExerciseList)

        lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                simpleItemTouchCallback.adapter.submitList(state.exercises)
                when (state) {
                    RoutineEditorState.Loading -> {
                        /* no-op */
                    }
                    is RoutineEditorState.Editing -> {
                        binding.apply {
                            editorToolbar.title = getString(R.string.editorTitleEdit)
                            if (firstRun) {
                                nameInput.setText(state.nameInputText)
                            }
                            nameInput.setSelection(state.nameInputText.length)
                            deleteButton.isVisible = true
                            saveButton.isVisible = true
                        }
                    }
                    is RoutineEditorState.New -> {
                        binding.editorToolbar.title = getString(R.string.editorTitleNew)
                        saveButton.isVisible = true
                        imm.showKeyboard()
                    }
                }
            }
        }
        return binding.root
    }

    private fun showDurationPicker(exerciseIndex: Int, duration: Long) {
        viewModel.indexPendingDurationChange = exerciseIndex
        findNavController().safeNavigate(
            RoutineEditorFragmentDirections.actionRoutineEditorFragmentToDurationPickerDialogFragment(
                duration
            )
        )
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

    fun startDragging(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FIRST_RUN_KEY, false)
    }
}