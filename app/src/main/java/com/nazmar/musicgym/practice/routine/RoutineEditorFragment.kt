package com.nazmar.musicgym.practice.routine

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.MainActivity
import com.nazmar.musicgym.R
import com.nazmar.musicgym.databinding.FragmentRoutineEditorBinding


class RoutineEditorFragment : Fragment() {

    private var _binding: FragmentRoutineEditorBinding? = null
    private val binding get() = _binding!!

    private lateinit var imm: InputMethodManager

    private val viewModel: RoutineEditorViewModel by viewModels {
        RoutineEditorViewModelFactory(
                arguments?.get(
                        "routineId"
                ) as Long, requireNotNull(this.activity).application
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).hideBottomNavBar()
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        _binding = FragmentRoutineEditorBinding.inflate(inflater)

        val adapter = RoutineExerciseAdapter()


        adapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY


        binding.routineExerciseList.adapter = adapter

        // Populate list with old routine
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
                imm.toggleSoftInput(
                        InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY
                )
            }
        }
        return binding.root
    }

//    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
//        observe(lifecycleOwner, object : Observer<T> {
//            override fun onChanged(t: T?) {
//                observer.onChanged(t)
//                removeObserver(this)
//            }
//        })
//    }


    private fun goBack() {
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        requireActivity().onBackPressed()
        (activity as MainActivity).showBottomNavBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}