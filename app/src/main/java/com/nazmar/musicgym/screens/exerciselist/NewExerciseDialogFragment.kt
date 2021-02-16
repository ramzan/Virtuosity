package com.nazmar.musicgym.screens.exerciselist

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nazmar.musicgym.R
import com.nazmar.musicgym.common.showKeyboard
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NewExerciseDialogFragment : DialogFragment() {
    @Inject
    lateinit var imm: InputMethodManager

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewModel: ExerciseListViewModel by activityViewModels()

        val layout = layoutInflater.inflate(R.layout.text_input_dialog, null)
        val text = layout.findViewById<EditText>(R.id.name_input)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.new_exercise)
            .setView(layout)
            .setPositiveButton(getString(R.string.dialog_positive_button_label)) { _, _ ->
                viewModel.addExercise(text.text.toString().trim())
            }
            .setNegativeButton(getString(R.string.dialog_negative_button_label)) { _, _ -> }
            .create()

        text.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                okButton.isEnabled = text.text.trim().isNotEmpty()
            }
        })

        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.isEnabled = text.text.trim().isNotEmpty()

            imm.showKeyboard()
            text.requestFocus()
        }
        return dialog
    }

}