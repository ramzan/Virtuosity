package com.nazmar.musicgym.practice.routine.editor

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.navGraphViewModels
import com.nazmar.musicgym.MAX_TIMER_DURATION
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
                    viewModel.updateDuration(exerciseIndex, l.coerceAtMost(MAX_TIMER_DURATION))

                },
                viewModel.getItemDuration(exerciseIndex),
                TimeDurationPicker.MM_SS
        )
    }
}