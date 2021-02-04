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
import java.time.Duration


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

        binding.routineExerciseList.adapter = simpleItemTouchCallback.adapter

        viewModel.oldExercises.observe(viewLifecycleOwner, {
            viewModel.loadOldRoutine()
            simpleItemTouchCallback.adapter.submitList(viewModel.currentExercises)
        })

        itemTouchHelper.attachToRecyclerView(binding.routineExerciseList)

        viewModel.updatedIndex.observe(viewLifecycleOwner) {
            simpleItemTouchCallback.adapter.notifyItemChanged(it)
        }


        binding.apply {
            editorToolbar.title =
                getString(if (viewModel.newRoutine) R.string.editorTitleNew else R.string.editorTitleEdit)
            editorToolbar.setNavigationOnClickListener {
                goBack()
            }

            val deleteButton = editorToolbar.menu.getItem(0)
            val saveButton = editorToolbar.menu.getItem(1)

            deleteButton.setOnMenuItemClickListener {
                showDeleteDialog()
                true
            }

            saveButton.setOnMenuItemClickListener {
                if (nameInput.text?.trim().isNullOrEmpty()) {
                    nameInput.setText("")
                    nameInputLayout.isErrorEnabled = true
                    nameInputLayout.error = getString(R.string.empty_name_error_msg)
                } else {
                    viewModel.updateRoutine()
                    goBack()
                }
                true
            }

            nameInput.doOnTextChanged { text, _, _, _ ->
                viewModel.nameInputText = text.toString().trim().replace('\n', ' ')
            }

            if (!viewModel.newRoutine) {
                viewModel.routine.observe(viewLifecycleOwner) {
                    if (it != null) {
                        deleteButton.isVisible = true
                        saveButton.isVisible = true
                        if (firstRun) {
                            nameInput.setText(it.name)
                            viewModel.nameInputText = it.name
                        }
                        nameInput.setSelection(viewModel.nameInputText.length)
                    } else if (viewModel.routineDeleted) goBack()
                }
            } else {
                saveButton.isVisible = true
                imm.showKeyboard()
            }

            exerciseSpinner.apply {
                onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
                    viewModel.addExercise(this.adapter.getItem(pos) as Exercise)
                    binding.routineExerciseList.adapter?.notifyItemInserted(viewModel.currentExercises.size)
                    setText("")
                }
            }

            // Populate exercise autocomplete
            viewModel.exercises.observe(viewLifecycleOwner, {
                exerciseSpinner.setAdapter(
                    ArrayAdapter(
                        requireContext(),
                        R.layout.list_item_exercise_spinner,
                        it
                    )
                )
            })

        }
        return binding.root
    }

    private fun showDurationPicker(exerciseIndex: Int, duration: Duration) {
        findNavController().navigate(
            RoutineEditorFragmentDirections.actionRoutineEditorToDurationPickerDialog(
                exerciseIndex,
                duration.toMillis()
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