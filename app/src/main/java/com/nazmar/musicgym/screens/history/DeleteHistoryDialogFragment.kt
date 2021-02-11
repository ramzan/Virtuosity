package com.nazmar.musicgym.screens.history

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nazmar.musicgym.R

class DeleteHistoryDialogFragment : DialogFragment() {

    private val viewModel: HistoryViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_history_dialog_message)
            .setPositiveButton(getString(R.string.dialog_positive_button_label)) { _, _ ->
                viewModel.deleteHistoryItem(requireArguments().getLong("historyId"))
            }
            .setNegativeButton(getString(R.string.dialog_negative_button_label)) { _, _ -> }
            .show()
    }
}