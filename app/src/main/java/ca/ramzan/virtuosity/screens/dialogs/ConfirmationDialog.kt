package ca.ramzan.virtuosity.screens.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.CONFIRMATION_RESULT
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ConfirmationDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(requireArguments().getInt("titleId"))
            .setMessage(requireArguments().getInt("messageId"))
            .setPositiveButton(requireArguments().getInt("positiveButtonMessage")) { _, _ ->
                setFragmentResult(
                    CONFIRMATION_RESULT,
                    bundleOf(requireArguments().getString("listenerKey", "") to true)
                )
            }
            .setNegativeButton(getString(R.string.dialog_negative_button_label)) { _, _ ->
                /* no-op */
            }
            .show()
    }
}