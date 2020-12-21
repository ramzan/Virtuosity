package com.nazmar.musicgym.exercises.detail

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.navigation.navGraphViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nazmar.musicgym.R
import com.nazmar.musicgym.showKeyboard

class RenameDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layout = layoutInflater.inflate(R.layout.text_input_dialog, null)
        val text = layout.findViewById<EditText>(R.id.name_input)

        val viewModel: ExerciseDetailViewModel by navGraphViewModels(R.id.exercisesGraph) {
            ExerciseDetailViewModelFactory(
                    arguments?.get(
                            "exerciseId"
                    ) as Long, requireNotNull(this.activity).application
            )
        }
        viewModel.exercise.observe(this) {
            if (it != null) {
                text.setText(it.name)
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.rename)
                .setView(layout)
                .setPositiveButton("OK") { _, _ ->
                    viewModel.renameExercise(text.text.toString().trim())
                }
                .setNegativeButton("CANCEL") { _, _ -> }
                .create()

        text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                okButton.isEnabled = text.text.isNotEmpty()
            }
        })

        dialog.setOnShowListener {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showKeyboard()
            text.requestFocus()
            text.setSelection(text.text.length)
        }
        return dialog
    }

}