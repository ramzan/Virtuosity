package com.nazmar.musicgym.screens.session

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.R
import com.nazmar.musicgym.common.*
import com.nazmar.musicgym.databinding.FragmentSessionBinding
import com.nazmar.musicgym.screens.BaseFragment
import com.nazmar.musicgym.session.timer.Timer
import com.nazmar.musicgym.session.timer.TimerService
import com.nazmar.musicgym.session.timer.TimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class SessionFragment : BaseFragment<FragmentSessionBinding>() {

    @Inject
    lateinit var imm: InputMethodManager

    @Inject
    lateinit var factory: SessionViewModel.Factory

    private val viewModel: SessionViewModel by viewModels {
        SessionViewModel.provideFactory(factory, requireArguments().getLong("routineId"))
    }

    private lateinit var timer: Timer
    private var bound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val timerService = service as TimerService.TimerBinder
            timer = timerService.getTimer()
            bound = true

            lifecycleScope.launchWhenStarted {
                timer.timeString.collect { string ->
                    binding.timer.text = string
                    binding.timerEditor.setText(string)
                }
            }

            lifecycleScope.launchWhenStarted {
                timer.status.collect { status ->
                    when (status) {
                        TimerState.RUNNING -> {
                            binding.pauseTimerButton.visibility = View.VISIBLE
                            binding.startTimerButton.visibility = View.GONE

                            binding.timerEditor.visibility = View.GONE
                            binding.timer.visibility = View.VISIBLE
                        }
                        TimerState.PAUSED, TimerState.STOPPED -> {
                            binding.pauseTimerButton.visibility = View.GONE
                            binding.startTimerButton.visibility = View.VISIBLE

                            binding.timerEditor.visibility = View.VISIBLE
                            binding.timer.visibility = View.GONE
                        }
                    }

                }
            }

            lifecycleScope.launchWhenStarted {
                viewModel.state.collect { state ->
                    when (state) {
                        is SessionState.PracticeScreen -> {
                            timerService.updateRoutineName(state.sessionName)
                            timer.setUpTimer(state.currentExercise)
                        }
                        else -> timer.clearExercise()
                    }
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(requireContext(), TimerService::class.java).also { intent ->
            if (isOreoOrAbove()) {
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        setFragmentResultListener(DURATION_PICKER_RESULT) { _, bundle ->
            timer.updateTimeLeft(bundle.getLong(DURATION_VALUE))
        }

    }

    override fun onStop() {
        super.onStop()
        requireContext().unbindService(connection)
        bound = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = goBack()
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().hideBottomNavBar()

        _binding = FragmentSessionBinding.inflate(inflater)

        binding.apply {
            nextExerciseButton.setOnClickListener {
                viewModel.nextExercise()
            }

            previousExerciseButton.setOnClickListener {
                viewModel.previousExercise()
            }

            bpmInput.doOnTextChanged { text, _, _, _ ->
                text?.let {
                    viewModel.updateBpm(it.toString().trimStart('0'))
                }
            }

            doneButton.setOnClickListener {
                viewModel.completeSession()
                goBack()
            }

            pauseTimerButton.setOnClickListener {
                if (bound) this@SessionFragment.timer.pauseTimer()
            }

            startTimerButton.setOnClickListener {
                if (bound) this@SessionFragment.timer.startTimer()
            }

            restartTimerButton.setOnClickListener {
                if (bound) this@SessionFragment.timer.restartTimer()
            }

            timerEditor.setOnClickListener {
                showTimerEditor()
            }

            sessionToolbar.setNavigationOnClickListener {
                goBack()
            }
        }

        val adapter = SummaryExerciseAdapter().also { adapter ->
            adapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

            binding.summaryList.adapter = adapter
        }

        lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is SessionState.PracticeScreen -> {
                        binding.apply {
                            sessionProgressBar.visibility = View.GONE
                            summaryView.visibility = View.GONE
                            exerciseView.visibility = View.VISIBLE

                            sessionToolbar.title = state.sessionName

                            previousExerciseButton.isEnabled = state.previousButtonEnabled
                            nextExerciseButton.visibility = View.VISIBLE
                            doneButton.visibility = View.GONE

                            sessionCurrentExerciseName.text = state.currentExerciseName
                            bpmInput.apply {
                                text =
                                    Editable.Factory.getInstance().newEditable(state.newExerciseBpm)
                                hint = state.currentExerciseBpmRecord
                                isEnabled = true
                                setSelection(text.length.coerceAtLeast(0))
                            }
                        }
                    }
                    is SessionState.SummaryScreen -> {
                        binding.sessionProgressBar.visibility = View.GONE
                        adapter.submitList(state.summaryList)
                        binding.nextExerciseButton.visibility = View.GONE
                        binding.doneButton.visibility = View.VISIBLE

                        binding.apply {
                            summaryView.visibility = View.VISIBLE
                            exerciseView.visibility = View.GONE
                            bpmInput.isEnabled = false
                        }
                    }
                    SessionState.Loading -> {
                        /* no-op */
                    }
                    SessionState.EmptyRoutine -> {
                        binding.apply {
                            sessionProgressBar.visibility = View.GONE
                            previousExerciseButton.visibility = View.GONE
                        }
                        viewModel.cancelSession()
                        findNavController().safeNavigate(
                            SessionFragmentDirections.actionSessionFragmentToInfoDialog(
                                R.string.empty_routine_dialog_title,
                                R.string.empty_routine_dialog_message
                            )
                        )
                    }
                }
            }
        }
        return binding.root
    }

    private fun goBack() {
        requireContext().stopService(Intent(requireContext(), TimerService::class.java))
        imm.hideKeyboard(requireView().windowToken)
        findNavController().popBackStack(R.id.routineListFragment, false)
    }

    private fun showTimerEditor() {
        findNavController().safeNavigate(
            SessionFragmentDirections.actionSessionFragmentToDurationPickerDialogFragment(
                timer.timeLeft.value ?: 0L
            )
        )
    }
}