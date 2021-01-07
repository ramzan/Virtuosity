package com.nazmar.musicgym.practice.routine.list

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.navGraphViewModels
import com.nazmar.musicgym.R
import com.nazmar.musicgym.practice.session.SessionViewModel
import com.nazmar.musicgym.practice.session.SessionViewModelFactory
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

        return TimeDurationPickerDialog(requireContext(),
                { _: TimeDurationPicker, l: Long ->
                    viewModel.updateEditorTIme(l.coerceAtMost(5999000L))
                },
                arguments?.getLong("timeLeft") ?: 0L,
                TimeDurationPicker.MM_SS
        )
    }
}