package com.nazmar.musicgym.practice.routine.editor

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.navGraphViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nazmar.musicgym.R

class DeleteRoutineDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewModel: RoutineEditorViewModel by navGraphViewModels(R.id.routineEditorGraph) {
            RoutineEditorViewModelFactory(arguments?.get("routineId") as Long)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_routine_dialog_message)
            .setPositiveButton(getString(R.string.dialog_positive_button_label)) { _, _ ->
                viewModel.deleteRoutine()
            }
            .setNegativeButton(getString(R.string.dialog_negative_button_label)) { _, _ -> }
            .show()
    }
}