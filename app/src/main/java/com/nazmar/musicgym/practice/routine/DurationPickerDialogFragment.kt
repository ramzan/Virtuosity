package com.nazmar.musicgym.practice.routine

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.nazmar.musicgym.db.RoutineExerciseName
import mobi.upod.timedurationpicker.TimeDurationPicker
import mobi.upod.timedurationpicker.TimeDurationPickerDialog

class DurationPickerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val exerciseIndex = requireArguments().get("exerciseIndex") as Int
        val duration = requireArguments().get("duration") as Long


        return TimeDurationPickerDialog(
            requireContext(),
            { _: TimeDurationPicker, l: Long ->
                val navController = findNavController()
                navController.previousBackStackEntry?.savedStateHandle?.set("minutes", l / 1000 / 60)
                navController.previousBackStackEntry?.savedStateHandle?.set("seconds", l / 1000 % 60)
                navController.previousBackStackEntry?.savedStateHandle?.set("exerciseIndex", exerciseIndex)

            },
            duration * 1000,
            TimeDurationPicker.MM_SS
        )
    }
}