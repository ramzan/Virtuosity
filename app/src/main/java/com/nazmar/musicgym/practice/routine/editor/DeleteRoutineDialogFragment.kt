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
            RoutineEditorViewModelFactory(
                arguments?.get(
                    "routineId"
                ) as Long, requireNotNull(this.activity).application
            )
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_routine_dialog_message)
            .setPositiveButton("OK") { _, _ ->
                viewModel.deleteRoutine()
            }
            .setNegativeButton("CANCEL") { _, _ -> }
            .show()
    }
}