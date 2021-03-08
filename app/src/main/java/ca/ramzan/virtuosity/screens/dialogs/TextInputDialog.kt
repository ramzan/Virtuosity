package ca.ramzan.virtuosity.screens.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.FIRST_RUN_KEY
import ca.ramzan.virtuosity.common.INPUT_TEXT
import ca.ramzan.virtuosity.common.TEXT_INPUT_RESULT
import ca.ramzan.virtuosity.common.showKeyboard
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val ARG_NAME_INPUT = "name_input"

@AndroidEntryPoint
class TextInputDialog : DialogFragment() {

    @Inject
    lateinit var imm: InputMethodManager
    private var nameInputText = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layout = layoutInflater.inflate(R.layout.text_input_dialog, null)
        val text = layout.findViewById<EditText>(R.id.name_input)

        val firstRun = savedInstanceState?.getBoolean(FIRST_RUN_KEY) ?: true

        nameInputText = if (firstRun) requireArguments().getString("initialText")
            .toString() else savedInstanceState?.getString(ARG_NAME_INPUT) ?: ""

        text.setText(nameInputText)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(requireArguments().getInt("titleId"))
            .setView(layout)
            .setPositiveButton(getString(R.string.dialog_positive_button_label)) { _, _ ->
                setFragmentResult(TEXT_INPUT_RESULT, bundleOf(INPUT_TEXT to nameInputText))
            }
            .setNegativeButton(getString(R.string.dialog_negative_button_label)) { _, _ ->
                /* no-op */
            }
            .create()

        text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                /* no-op */
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                /* no-op */
            }

            override fun afterTextChanged(p0: Editable?) {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                with(text.text.trim()) {
                    okButton.isEnabled = this.isNotEmpty()
                    nameInputText = this.toString().replace('\n', ' ')
                }
            }
        })

        dialog.setOnShowListener {
            imm.showKeyboard()
            text.requestFocus()
            text.setSelection(text.text.length)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = text.text.isNotEmpty()
        }
        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FIRST_RUN_KEY, false)
        outState.putString(ARG_NAME_INPUT, nameInputText)
    }
}