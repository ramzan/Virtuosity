package com.nazmar.musicgym.screens.routinelist

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.R
import com.nazmar.musicgym.common.*
import com.nazmar.musicgym.databinding.FragmentRoutineListBinding
import com.nazmar.musicgym.routine.Routine
import com.nazmar.musicgym.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RoutineListFragment : BaseFragment<FragmentRoutineListBinding>() {

    @Inject
    lateinit var prefs: SharedPreferences

    private val viewModel: RoutineListViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        setFragmentResultListener(CONFIRMATION_RESULT) { _, bundle ->
            if (bundle.getBoolean(POSITIVE_RESULT)) {
                viewModel.useCase.clearSavedSession()
                // TODO why isn't this navigating
                viewModel.sessionToStartId?.let { startSession(it) }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentRoutineListBinding.inflate(inflater)

        requireActivity().showBottomNavBar()

        if (prefs.contains(SAVED_SESSION_ID)) showSavedSessionCard() else hideSavedSessionCard()

        RoutineAdapter(
            object : RoutineAdapter.OnClickListener {
                override fun onEdit(routine: Routine) = showRoutineEditor(routine.id)

                override fun onStart(routine: Routine) = checkSessionSaved(routine.id)
            }
        ).run {
            this.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

            binding.routineList.adapter = this

            viewModel.routines.observe(viewLifecycleOwner, {
                this.submitList(it)
            })
        }

        binding.fab.setOnClickListener {
            showRoutineEditor(0)
        }

        binding.resumeSessionBtn.setOnClickListener {
            with(prefs.getLong(SAVED_SESSION_ID, 0)) {
                if (this > 0) startSession(this)
            }

        }

        return binding.root
    }

    private fun showSavedSessionCard() {
        binding.apply {
            savedSessionCard.visibility = View.VISIBLE
            savedSessionName.text = prefs.getString(SAVED_SESSION_NAME, "")
            savedSessionDate.text = Date(
                prefs.getLong(
                    SAVED_SESSION_TIME,
                    System.currentTimeMillis()
                )
            ).toString()
            resumeSessionBtn.isEnabled = true

        }
    }

    private fun hideSavedSessionCard() {
        binding.apply {
            savedSessionCard.visibility = View.GONE
            savedSessionName.text = ""
            savedSessionDate.text = ""
            resumeSessionBtn.isEnabled = false
        }
    }

    private fun showRoutineEditor(id: Long) {
        findNavController().safeNavigate(
            RoutineListFragmentDirections.actionRoutineListFragmentToRoutineEditorFragment(id)
        )
    }

    private fun startSession(id: Long) {
        findNavController().safeNavigate(
            RoutineListFragmentDirections.actionRoutineListFragmentToSessionFragment(id)
        )
    }

    private fun checkSessionSaved(id: Long) {
        if (prefs.contains(SAVED_SESSION_NAME)) {
            viewModel.sessionToStartId = id
            findNavController().safeNavigate(
                RoutineListFragmentDirections.actionRoutineListFragmentToConfirmationDialog(
                    R.string.clear_session_dialog_message
                )
            )
        } else startSession(id)
    }
}