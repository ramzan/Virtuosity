package com.nazmar.musicgym.exercises

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nazmar.musicgym.R
import com.nazmar.musicgym.databinding.FragmentExerciseDetailBinding
import com.nazmar.musicgym.hideBottomNavBar
import com.nazmar.musicgym.showBottomNavBar


class ExerciseDetailFragment : DialogFragment() {

    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExerciseDetailViewModel by viewModels {
        ExerciseDetailViewModelFactory(
                arguments?.get(
                        "exerciseId"
                ) as Long, requireNotNull(this.activity).application
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.ExerciseDetailDialog)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        requireActivity().hideBottomNavBar()

        _binding = FragmentExerciseDetailBinding.inflate(inflater)

        binding.editorToolbar.apply {
            setNavigationOnClickListener {
                goBack()
            }

            viewModel.exercise.observe(viewLifecycleOwner) {
                binding.apply {
                    editorToolbar.title = it?.name ?: ""
                    menu.getItem(0).isEnabled = it !== null
                    menu.getItem(1).isEnabled = it !== null
                }
            }

            binding.apply {

                // Rename button
                menu.getItem(0).setOnMenuItemClickListener {
                    showRenameDialog()
                    true
                }

                // Delete button
                menu.getItem(1).setOnMenuItemClickListener {
                    showDeleteDialog()
                    true
                }
            }
        }
        return binding.root
    }

    private fun showDeleteDialog(): Boolean {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_dialog_message)
                .setPositiveButton("OK") { _, _ ->
                    goBack()
                    viewModel.deleteExercise()
                }
                .setNegativeButton("CANCEL") { _, _ -> }
                .show()
        return true
    }

    private fun showRenameDialog(): Boolean {
        val layout = layoutInflater.inflate(R.layout.text_input_dialog, null)
        val text = layout.findViewById<EditText>(R.id.name_input)
        text.setText(viewModel.exercise.value!!.name)

        val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.rename)
                .setView(layout)
                .setPositiveButton("OK") { _, _ ->
                    viewModel.renameExercise(text.text.toString().trim())
                }
                .setNegativeButton("CANCEL") { _, _ -> }
                .create()

        text.addTextChangedListener(object : TextWatcher {
            private fun handleText() {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                okButton.isEnabled = text.text.isNotEmpty()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                handleText()
            }
        })
        dialog.show()
        return true
    }

    private fun goBack() {
        requireActivity().onBackPressed()
        requireActivity().showBottomNavBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}