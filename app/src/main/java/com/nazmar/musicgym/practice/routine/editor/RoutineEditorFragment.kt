package com.nazmar.musicgym.practice.routine.editor

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.*
import com.nazmar.musicgym.databinding.FragmentRoutineEditorBinding


class RoutineEditorFragment : Fragment() {

    private var _binding: FragmentRoutineEditorBinding? = null
    private val binding get() = _binding!!

    private lateinit var imm: InputMethodManager

    private val viewModel: RoutineEditorViewModel by navGraphViewModels(R.id.routineEditorGraph) {
        RoutineEditorViewModelFactory(
                arguments?.get(
                        "routineId"
                ) as Long, requireNotNull(this.activity).application
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().hideBottomNavBar()

        imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        _binding = FragmentRoutineEditorBinding.inflate(inflater)

        val adapter = RoutineExerciseAdapter(::showDurationPicker)

        adapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.routineExerciseList.adapter = adapter

        viewModel.oldExercises.observe(viewLifecycleOwner, {
            viewModel.loadOldRoutine()
            adapter.submitList(viewModel.currentExercises)
        })

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
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
        })

        itemTouchHelper.attachToRecyclerView(binding.routineExerciseList)

        viewModel.updatedIndex.observe(viewLifecycleOwner) {
            adapter.notifyItemChanged(it)
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
                viewModel.deleteRoutine()
                goBack()
                true
            }

            saveButton.setOnMenuItemClickListener {
                if (TextUtils.isEmpty(nameInput.text?.trim())) {
                    nameInput.setText("")
                    nameInputLayout.isErrorEnabled = true
                    nameInputLayout.error = getString(R.string.empty_name_error_msg)
                } else {
                    viewModel.updateRoutine(nameInput.text.toString())
                    goBack()
                }
                true
            }


            if (!viewModel.newRoutine) {
                viewModel.routine.observe(viewLifecycleOwner) {
                    if (it != null) {
                        deleteButton.isVisible = true
                        saveButton.isVisible = true
                        nameInput.setText(it.name)
                        nameInput.setSelection(it.name.length)
                    }
                }
            } else {
                saveButton.isVisible = true
                // Show the keyboard.
                imm.showKeyboard()
            }

            exerciseSpinner.apply {
                onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
                    viewModel.addExercise(pos)
                    binding.routineExerciseList.adapter?.notifyItemInserted(viewModel.currentExercises.size)
                    setText("")
                }
            }

            // Populate tag autocomplete with all preexisting tags
            viewModel.exercises.observe(viewLifecycleOwner, {
                exerciseSpinner.setAdapter(
                        ArrayAdapter(
                                requireContext(),
                                R.layout.list_item_exercise_spinner,
                                it.map { e -> e.name }
                        )
                )
            })

        }
        return binding.root
    }

    private fun showDurationPicker(exerciseIndex: Int, duration: Long) {
        val action =
                RoutineEditorFragmentDirections.actionRoutineEditorToDurationPickerDialog(
                        exerciseIndex,
                        duration
                )
        findNavController().navigate(action)
    }

    private fun goBack() {
        imm.hideKeyboard(requireView().windowToken)
        requireActivity().onBackPressed()
        requireActivity().showBottomNavBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().showBottomNavBar()
        _binding = null
    }
}