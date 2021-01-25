package com.nazmar.musicgym.practice.routine.list

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nazmar.musicgym.R
import com.nazmar.musicgym.data.Repository

class RestartSessionDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_session_dialog_message)
            .setPositiveButton(getString(R.string.dialog_positive_button_label)) { _, _ ->
                Repository.clearSavedSession()
                findNavController().navigate(
                    RestartSessionDialogFragmentDirections
                        .actionRestartSessionDialogFragmentToSessionGraph(
                            requireArguments().getLong("routineId")
                        )
                )
            }
            .setNegativeButton(getString(R.string.dialog_negative_button_label)) { _, _ -> }
            .show()
    }
}