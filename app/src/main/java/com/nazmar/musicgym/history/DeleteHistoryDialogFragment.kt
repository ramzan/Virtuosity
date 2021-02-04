package com.nazmar.musicgym.history

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nazmar.musicgym.R

const val ARG_HISTORY_ID = "history_id"

class DeleteHistoryDialogFragment : DialogFragment() {

    private val viewModel: HistoryViewModel by activityViewModels {
        HistoryViewModelFactory()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_history_dialog_message)
            .setPositiveButton(getString(R.string.dialog_positive_button_label)) { _, _ ->
                viewModel.deleteHistoryItem(requireArguments().getLong(ARG_HISTORY_ID))
            }
            .setNegativeButton(getString(R.string.dialog_negative_button_label)) { _, _ -> }
            .show()
    }
}