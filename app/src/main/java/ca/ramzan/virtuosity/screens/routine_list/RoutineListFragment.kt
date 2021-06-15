package ca.ramzan.virtuosity.screens.routine_list

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.*
import ca.ramzan.virtuosity.databinding.FragmentRoutineListBinding
import ca.ramzan.virtuosity.screens.BaseFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject


@AndroidEntryPoint
class RoutineListFragment : BaseFragment<FragmentRoutineListBinding>() {

    @Inject
    lateinit var prefs: SharedPreferences

    private val viewModel: RoutineListViewModel by viewModels()

    private lateinit var adapter: RoutineListCardAdapter

    override fun onStart() {
        super.onStart()
        requireActivity().showBottomNavBar()
        setFragmentResultListener(CONFIRMATION_RESULT) { _, bundle ->
            if (bundle.getBoolean(CLEAR_SESSION_AND_START)) {
                viewModel.sessionToStartId?.let {
                    viewModel.useCase.clearSavedSession()
                    findNavController().popBackStack(R.id.routineListFragment, false)
                    startSession(it)
                }
            } else if (bundle.getBoolean(CLEAR_SESSION)) {
                viewModel.useCase.clearSavedSession()
                (viewModel.state.value as? RoutineListState.Loaded)?.run {
                    adapter.submitListWithHeader(routineCards)
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

        setUpBinding(FragmentRoutineListBinding.inflate(inflater))

        adapter = RoutineListCardAdapter(
            object : RoutineListCardAdapter.OnClickListener {
                override fun onCreateRoutine() = showRoutineEditor(0)

                override fun onEditRoutine(routine: RoutineListCard.RoutineCard) =
                    showRoutineEditor(routine.id)

                override fun onStartSession(routine: RoutineListCard.RoutineCard) =
                    checkSessionSaved(routine.id)

                override fun onResumeSession() = resumeSession()

                override fun onCancelSession() = cancelSession(CLEAR_SESSION)
            }
        )

        binding.routineList.adapter = adapter

        binding.routineListToolbar.menu.findItem(R.id.settings).setOnMenuItemClickListener {
            goToSettings()
            true
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    RoutineListState.Loading -> {
                        binding.routineList.visibility = View.GONE
                        binding.routineListProgressBar.visibility = View.VISIBLE
                    }

                    is RoutineListState.Loaded -> {
                        binding.routineList.visibility = View.VISIBLE
                        binding.routineListProgressBar.visibility = View.GONE
                        if (prefs.contains(SAVED_SESSION_ID)) {
                            adapter.submitListWithSavedSession(
                                state.routineCards,
                                prefs.getString(SAVED_SESSION_NAME, "").toString(),
                                prefs.getLong(SAVED_SESSION_TIME, System.currentTimeMillis())
                            )
                        } else adapter.submitListWithHeader(state.routineCards)
                    }
                }
            }
        }

        if (requireArguments().getBoolean("routineDeleted")) {
            Snackbar.make(
                requireActivity().findViewById(R.id.nav_view),
                getString(R.string.routine_deleted_message),
                Snackbar.LENGTH_SHORT
            )
                .setAnchorView(requireActivity().findViewById(R.id.nav_view))
                .show()
            requireArguments().remove("routineDeleted")
        }

        return binding.root
    }

    private fun goToSettings() {
        findNavController().safeNavigate(
            RoutineListFragmentDirections.actionRoutineListFragmentToSettingsFragment()
        )
    }

    private fun cancelSession(listenerKey: String) {
        findNavController().safeNavigate(
            RoutineListFragmentDirections.actionRoutineListFragmentToConfirmationDialog(
                R.string.clear_session_dialog_title,
                R.string.clear_session_dialog_message,
                R.string.clear,
                listenerKey
            )
        )
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
            cancelSession(CLEAR_SESSION_AND_START)
        } else startSession(id)
    }
}