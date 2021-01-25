package com.nazmar.musicgym.exercises.detail

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.navigation.navGraphViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nazmar.musicgym.FIRST_RUN_KEY
import com.nazmar.musicgym.R
import com.nazmar.musicgym.getInputMethodManager
import com.nazmar.musicgym.showKeyboard

class RenameDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layout = layoutInflater.inflate(R.layout.text_input_dialog, null)
        val text = layout.findViewById<EditText>(R.id.name_input)

        val viewModel: ExerciseDetailViewModel by navGraphViewModels(R.id.exercisesGraph) {
            ExerciseDetailViewModelFactory(requireArguments().getLong("exerciseId"))
        }

        val firstRun = savedInstanceState?.getBoolean(FIRST_RUN_KEY) ?: true

        if (firstRun) {
            viewModel.nameInputText = viewModel.exercise.value?.name ?: ""
        }

        text.setText(viewModel.nameInputText)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.rename)
            .setView(layout)
            .setPositiveButton(getString(R.string.dialog_positive_button_label)) { _, _ ->
                viewModel.renameExercise()
            }
            .setNegativeButton(getString(R.string.dialog_negative_button_label)) { _, _ -> }
            .create()

        text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                with(text.text.trim()) {
                    okButton.isEnabled = this.isNotEmpty()
                    viewModel.nameInputText = this.toString().replace('\n', ' ')
                }
            }
        })

        dialog.setOnShowListener {
            requireActivity().getInputMethodManager().showKeyboard()
            text.requestFocus()
            text.setSelection(text.text.length)
        }
        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FIRST_RUN_KEY, false)
    }
}