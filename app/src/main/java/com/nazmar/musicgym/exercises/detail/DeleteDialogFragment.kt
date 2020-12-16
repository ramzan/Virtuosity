package com.nazmar.musicgym.exercises.detail

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.navGraphViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nazmar.musicgym.R

class DeleteDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewModel: ExerciseDetailViewModel by navGraphViewModels(R.id.exercisesGraph) {
            ExerciseDetailViewModelFactory(
                    arguments?.get(
                            "exerciseId"
                    ) as Long, requireNotNull(this.activity).application
            )
        }

        return MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_dialog_message)
                .setPositiveButton("OK") { _, _ ->
                    viewModel.deleteExercise()
                }
                .setNegativeButton("CANCEL") { _, _ -> }
                .show()
    }
}