package com.nazmar.musicgym.screens.session

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.navGraphViewModels
import com.nazmar.musicgym.R
import com.nazmar.musicgym.common.MAX_TIMER_DURATION
import mobi.upod.timedurationpicker.TimeDurationPicker
import mobi.upod.timedurationpicker.TimeDurationPickerDialog

class TimerEditorDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val viewModel: SessionViewModel by navGraphViewModels(R.id.sessionGraph)

        return TimeDurationPickerDialog(
            requireContext(),
            { _: TimeDurationPicker, l: Long ->
                viewModel.updateEditorTime(l.coerceAtMost(MAX_TIMER_DURATION))
            },
            requireArguments().getLong("timeLeft"),
            TimeDurationPicker.HH_MM_SS
        )
    }
}