package com.nazmar.musicgym.exercises.detail

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.navGraphViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nazmar.musicgym.R

class DeleteExerciseDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewModel: ExerciseDetailViewModel by navGraphViewModels(R.id.exercisesGraph) {
            ExerciseDetailViewModelFactory(arguments?.get("exerciseId") as Long)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_exercise_dialog_message)
            .setPositiveButton(getString(R.string.dialog_positive_button_label)) { _, _ ->
                viewModel.deleteExercise()
            }
            .setNegativeButton(getString(R.string.dialog_negative_button_label)) { _, _ -> }
            .show()
    }
}