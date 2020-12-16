package com.nazmar.musicgym.exercises.list

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nazmar.musicgym.R

class NewExerciseDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewModel: ExerciseListViewModel by activityViewModels {
            ExerciseListViewModelFactory(
                    requireNotNull(this.activity).application
            )
        }

        val layout = layoutInflater.inflate(R.layout.text_input_dialog, null)
        val text = layout.findViewById<EditText>(R.id.name_input)

        val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.new_exercise)
                .setView(layout)
                .setPositiveButton("OK") { _, _ ->
                    viewModel.addExercise(text.text.toString().trim())
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
        return dialog
    }

}