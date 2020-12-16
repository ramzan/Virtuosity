package com.nazmar.musicgym.practice.routine

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.navGraphViewModels
import com.nazmar.musicgym.R
import mobi.upod.timedurationpicker.TimeDurationPicker
import mobi.upod.timedurationpicker.TimeDurationPickerDialog

class DurationPickerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val viewModel: RoutineEditorViewModel by navGraphViewModels(R.id.routineEditorGraph) {
            RoutineEditorViewModelFactory(
                    arguments?.get(
                            "routineId"
                    ) as Long, requireNotNull(this.activity).application
            )
        }

        val exerciseIndex = requireArguments().getInt("exerciseIndex")

        return TimeDurationPickerDialog(
                requireContext(),
                { _: TimeDurationPicker, l: Long ->
                    viewModel.updateDuration(exerciseIndex, l / 1000 / 60, l / 1000 % 60)

                },
                viewModel.getItemDuration(exerciseIndex) * 1000,
                TimeDurationPicker.MM_SS
        )
    }
}