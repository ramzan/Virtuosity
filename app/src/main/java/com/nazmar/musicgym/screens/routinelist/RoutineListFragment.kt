package com.nazmar.musicgym.screens.routinelist

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.R
import com.nazmar.musicgym.common.*
import com.nazmar.musicgym.databinding.FragmentRoutineListBinding
import com.nazmar.musicgym.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class RoutineListFragment : BaseFragment<FragmentRoutineListBinding>() {

    @Inject
    lateinit var prefs: SharedPreferences

    private val viewModel: RoutineListViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        requireActivity().showBottomNavBar()
        setFragmentResultListener(CONFIRMATION_RESULT) { _, bundle ->
            if (bundle.getBoolean(POSITIVE_RESULT)) {
                viewModel.sessionToStartId?.let {
                    viewModel.useCase.clearSavedSession()
                    findNavController().popBackStack(R.id.routineListFragment, false)
                    startSession(it)
                }
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

        val adapter = RoutineListCardAdapter(
            object : RoutineListCardAdapter.OnClickListener {
                override fun onEdit(routine: RoutineListCard.RoutineCard) =
                    showRoutineEditor(routine.id)

                override fun onStart(routine: RoutineListCard.RoutineCard) =
                    checkSessionSaved(routine.id)

                override fun onResumeSession() = resumeSession()
            }
        )
        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.routineList.adapter = adapter

        lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    RoutineListState.Loading -> {
                        binding.routineList.visibility = View.GONE
                        binding.routineListProgressBar.visibility = View.VISIBLE
                        delay(1000)
                    }

                    is RoutineListState.Loaded -> {
                        binding.routineList.visibility = View.VISIBLE
                        binding.routineListProgressBar.visibility = View.GONE

                        if (prefs.contains(SAVED_SESSION_ID)) {
                            adapter.addSavedSessionCardAndSubmitList(
                                state.routineCards,
                                prefs.getString(SAVED_SESSION_NAME, "").toString(),
                                prefs.getLong(SAVED_SESSION_TIME, System.currentTimeMillis())
                            )
                        } else adapter.submitList(state.routineCards)
                    }
                }
            }
        }

        binding.fab.setOnClickListener {
            showRoutineEditor(0)
        }

        return binding.root
    }

    override fun onDestroyView() {
        binding.routineList.adapter = null
        super.onDestroyView()
    }

    private fun showRoutineEditor(id: Long) {
        findNavController().safeNavigate(
            RoutineListFragmentDirections.actionRoutineListFragmentToRoutineEditorFragment(id)
        )
    }

    private fun resumeSession() {
        prefs.getLong(SAVED_SESSION_ID, 0).run {
            if (this > 0) startSession(this)
        }
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