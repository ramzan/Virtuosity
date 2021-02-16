package com.nazmar.musicgym.screens.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.nazmar.musicgym.common.DURATION_PICKER_RESULT
import com.nazmar.musicgym.common.DURATION_VALUE
import com.nazmar.musicgym.common.MAX_TIMER_DURATION
import mobi.upod.timedurationpicker.TimeDurationPicker
import mobi.upod.timedurationpicker.TimeDurationPickerDialog

class DurationPickerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimeDurationPickerDialog(
            requireContext(),
            { _: TimeDurationPicker, l: Long ->
                setFragmentResult(
                    DURATION_PICKER_RESULT,
                    bundleOf(DURATION_VALUE to l.coerceAtMost(MAX_TIMER_DURATION))
                )

            },
            requireArguments().getLong("initialDuration"),
            TimeDurationPicker.HH_MM_SS
        )
    }
}