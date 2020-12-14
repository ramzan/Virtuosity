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
import com.nazmar.musicgym.MainActivity
import com.nazmar.musicgym.R
import com.nazmar.musicgym.databinding.FragmentExerciseViewBinding


class ExerciseViewFragment : DialogFragment() {

    private var _binding: FragmentExerciseViewBinding? = null
    private val binding get() = _binding!!

    private val eVViewModel: ExerciseViewViewModel by viewModels {
        ExerciseViewViewModelFactory(
            arguments?.get(
                "exerciseId"
            ) as Long, requireNotNull(this.activity).application
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).hideBottomNavBar()
        setStyle(STYLE_NORMAL, R.style.ExerciseViewDialog)
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

        _binding = FragmentExerciseViewBinding.inflate(inflater)

        binding.editorToolbar.apply {
            setNavigationOnClickListener {
                goBack()
            }

            eVViewModel.exercise.observe(viewLifecycleOwner) {
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
                eVViewModel.deleteExercise()
            }
            .setNegativeButton("CANCEL") { _, _ -> }
            .show()
        return true
    }

    private fun showRenameDialog(): Boolean {
        val layout = layoutInflater.inflate(R.layout.text_input_dialog, null)
        val text = layout.findViewById<EditText>(R.id.name_input)
        text.setText(eVViewModel.exercise.value!!.name)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.rename)
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                eVViewModel.renameExercise(text.text.toString().trim())
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
        (activity as MainActivity).showBottomNavBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}