package ca.ramzan.virtuosity.screens.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.session.timer.TimerService

class InfoDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(requireArguments().getInt("titleId"))
            .setMessage(requireArguments().getInt("messageId"))
            .setPositiveButton(getString(R.string.dialog_positive_button_label)) { _, _ ->
                /* no-op */
            }
            .show()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireContext().stopService(Intent(requireContext(), TimerService::class.java))
        findNavController().popBackStack(R.id.routineListFragment, false)
    }
}