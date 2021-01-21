package com.nazmar.musicgym.practice.session

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.navGraphViewModels
import com.nazmar.musicgym.MAX_TIMER_DURATION
import com.nazmar.musicgym.R
import mobi.upod.timedurationpicker.TimeDurationPicker
import mobi.upod.timedurationpicker.TimeDurationPickerDialog

class TimerEditorDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val viewModel: SessionViewModel by navGraphViewModels(R.id.sessionGraph) {
            SessionViewModelFactory(
                arguments?.get(
                    "routineId"
                ) as Long, requireNotNull(this.activity).application
            )
        }

        return TimeDurationPickerDialog(
            requireContext(),
            { _: TimeDurationPicker, l: Long ->
                viewModel.updateEditorTime(l.coerceAtMost(MAX_TIMER_DURATION))
            },
            arguments?.getLong("timeLeft") ?: 0L,
            TimeDurationPicker.MM_SS
        )
    }
}