package com.nazmar.musicgym.practice.routine

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        _binding = FragmentRoutineEditorBinding.inflate(inflater)

        val adapter = RoutineExerciseAdapter()


        adapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY


        binding.routineExerciseList.adapter = adapter

        val iTH = ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
                    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                        val fromPos = viewHolder.bindingAdapterPosition
                        val toPos = target.bindingAdapterPosition
                        Log.d("zot", "$fromPos -> $toPos")
                        adapter.submitList(viewModel.move(fromPos, toPos))
                        return true // true if moved, false otherwise
                    }


                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        // remove from adapter
                    }
                })

        iTH.attachToRecyclerView(binding.routineExerciseList)

        viewModel.routineExercises.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        binding.apply {
            editorToolbar.title = getString(if (viewModel.newRoutine) R.string.editorTitleNew else R.string.editorTitleEdit)
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
                viewModel.routine.observeOnce(viewLifecycleOwner) {
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

            viewModel.routineExercises.observe(viewLifecycleOwner) {
                Log.d("zot", it.toString())
            }


        }
        return binding.root
    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }


    private fun goBack() {
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        requireActivity().onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}