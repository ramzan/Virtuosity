package ca.ramzan.virtuosity.screens.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.CONFIRMATION_RESULT
import ca.ramzan.virtuosity.common.POSITIVE_RESULT


class ConfirmationDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(requireArguments().getInt("titleId"))
            .setPositiveButton(getString(R.string.dialog_positive_button_label)) { _, _ ->
                setFragmentResult(CONFIRMATION_RESULT, bundleOf(POSITIVE_RESULT to true))
            }
            .setNegativeButton(getString(R.string.dialog_negative_button_label)) { _, _ ->
                /* no-op */
            }
            .show()
    }
}