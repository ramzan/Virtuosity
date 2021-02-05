package com.nazmar.musicgym.routine.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.*
import com.nazmar.musicgym.databinding.FragmentRoutineEditorBinding
import com.nazmar.musicgym.db.Exercise


class RoutineEditorFragment : Fragment() {

    private var _binding: FragmentRoutineEditorBinding? = null
    private val binding get() = _binding!!

    private lateinit var imm: InputMethodManager

    private val viewModel: RoutineEditorViewModel by navGraphViewModels(R.id.routineEditorGraph) {
        RoutineEditorViewModelFactory(requireArguments().getLong("routineId"))
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
                if (viewHolder.itemViewType != target.itemViewType) {
                    return false
                }
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition
                viewModel.state.value?.moveItem(fromPos, toPos)
                adapter.notifyItemMoved(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.state.value?.deleteItem(viewHolder.bindingAdapterPosition)
                adapter.notifyItemRemoved(viewHolder.bindingAdapterPosition)
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (actionState != ACTION_STATE_IDLE) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().hideBottomNavBar()

        val firstRun = savedInstanceState?.getBoolean(FIRST_RUN_KEY) ?: true

        imm = requireActivity().getInputMethodManager()

        _binding = FragmentRoutineEditorBinding.inflate(inflater)

        binding.apply {
            editorToolbar.setNavigationOnClickListener {
                goBack()
            }

            nameInput.doOnTextChanged { text, _, _, _ ->
                viewModel.state.value?.nameInputText = text.toString().trim().replace('\n', ' ')
            }

            exerciseSpinner.apply {
                onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
                    viewModel.state.value?.let {
                        it.addExercise(this.adapter.getItem(pos) as Exercise)
                        binding.routineExerciseList.adapter?.notifyItemInserted(it.exercises.size)
                        setText("")
                    }
                }
            }

            // Populate exercise autocomplete
            viewModel.allExercises.observe(viewLifecycleOwner, {
                exerciseSpinner.setAdapter(
                    ArrayAdapter(
                        requireContext(),
                        R.layout.list_item_exercise_spinner,
                        it
                    )
                )
            })
        }

        binding.routineExerciseList.adapter = simpleItemTouchCallback.adapter
        itemTouchHelper.attachToRecyclerView(binding.routineExerciseList)

        viewModel.exercises.observe(viewLifecycleOwner) {
            simpleItemTouchCallback.adapter.submitList(it)
        }

        val deleteButton = binding.editorToolbar.menu.getItem(0)
        val saveButton = binding.editorToolbar.menu.getItem(1)

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                RoutineEditorState.Loading -> {
                }
                is RoutineEditorState.Editing -> {
                    binding.apply {
                        editorToolbar.title = getString(R.string.editorTitleEdit)

                        if (firstRun) {
                            nameInput.setText(state.nameInputText)
                        }
                        nameInput.setSelection(state.nameInputText.length)

                        saveButton.setOnMenuItemClickListener {
                            if (nameInput.text?.trim().isNullOrEmpty()) {
                                nameInput.setText("")
                                nameInputLayout.isErrorEnabled = true
                                nameInputLayout.error = getString(R.string.empty_name_error_msg)
                            } else {
                                state.saveRoutine()
                                goBack()
                            }
                            true
                        }
                        deleteButton.setOnMenuItemClickListener {
                            showDeleteDialog()
                            true
                        }
                        deleteButton.isVisible = true
                        saveButton.isVisible = true
                    }
                }
                is RoutineEditorState.New -> {
                    binding.apply {
                        editorToolbar.title = getString(R.string.editorTitleNew)

                        saveButton.setOnMenuItemClickListener {
                            if (nameInput.text?.trim().isNullOrEmpty()) {
                                nameInput.setText("")
                                nameInputLayout.isErrorEnabled = true
                                nameInputLayout.error = getString(R.string.empty_name_error_msg)
                            } else {
                                state.saveRoutine()
                                goBack()
                            }
                            true
                        }
                    }
                    saveButton.isVisible = true
                    imm.showKeyboard()
                }
                RoutineEditorState.Deleted -> goBack()
            }
        }
        return binding.root
    }

    private fun showDurationPicker(exerciseIndex: Int, duration: Long) {
        findNavController().navigate(
            RoutineEditorFragmentDirections.actionRoutineEditorToDurationPickerDialog(
                exerciseIndex,
                duration
            )
        )
    }

    private fun showDeleteDialog() {
        findNavController().navigate(
            RoutineEditorFragmentDirections.actionRoutineEditorToDeleteRoutineDialogFragment(
                requireArguments().getLong("routineId")
            )
        )
    }

    private fun goBack() {
        imm.hideKeyboard(requireView().windowToken)
        findNavController().popBackStack(R.id.routineListFragment, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().showBottomNavBar()
        _binding = null
    }

    fun startDragging(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FIRST_RUN_KEY, false)
    }
}