package ca.ramzan.virtuosity.screens.session

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Resources
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
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.*
import ca.ramzan.virtuosity.databinding.FragmentSessionBinding
import ca.ramzan.virtuosity.screens.BaseFragment
import ca.ramzan.virtuosity.session.timer.Timer
import ca.ramzan.virtuosity.session.timer.TimerService
import ca.ramzan.virtuosity.session.timer.TimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.lang.Integer.min
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

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                timer.timeString.collect { string ->
                    binding.timer.text = string
                    binding.timerEditor.setText(string)
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                timer.timeLeft.collect { timeLeft ->
                    binding.timerProgressBar.setProgressCompat(timeLeft?.toInt() ?: 0, true)
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                timer.status.collect { status ->
                    when (status) {
                        TimerState.RUNNING -> {
                            binding.pausePlayButton.apply {
                                setImageResource(R.drawable.ic_baseline_pause_24)
                                setOnClickListener {
                                    if (bound) this@SessionFragment.timer.pauseTimer()
                                }
                            }

                            binding.timerEditor.visibility = View.INVISIBLE
                            binding.timer.visibility = View.VISIBLE
                        }
                        TimerState.PAUSED, TimerState.STOPPED -> {
                            binding.pausePlayButton.apply {
                                setImageResource(R.drawable.ic_baseline_play_arrow_24)
                                setOnClickListener {
                                    if (bound) this@SessionFragment.timer.startTimer()
                                }
                            }

                            binding.timerEditor.visibility = View.VISIBLE
                            binding.timer.visibility = View.INVISIBLE
                        }
                    }

                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.state.collect { state ->
                    if (state is SessionState.PracticeScreen) {
                        timerService.updateRoutineName(state.sessionName)
                        timer.setUpTimer(state.currentExercise)
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
            bundle.getLong(DURATION_VALUE).run {
                timer.updateTimeLeft(this)
                binding.timerProgressBar.max = this.toInt()
                binding.timerProgressBar.setProgressCompat(this.toInt(), true)

            }
        }
        setFragmentResultListener(CONFIRMATION_RESULT) { _, bundle ->
            if (bundle.getBoolean(FINISH_SESSION)) {
                viewModel.completeSession()
                gotToSummary()
            }
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
            }
        )
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

            sessionToolbar.menu.getItem(0).setOnMenuItemClickListener {
                confirmFinishSession()
                true
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

            timerProgressBar.indicatorSize = Resources.getSystem().displayMetrics.run {
                min(widthPixels, heightPixels) / 4 * 3
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is SessionState.PracticeScreen -> {
                        binding.apply {
                            sessionProgressBar.visibility = View.GONE

                            sessionToolbar.title = state.sessionName

                            previousExerciseButton.isEnabled = state.previousButtonEnabled
                            nextExerciseButton.isEnabled = state.nextButtonEnabled

                            sessionCurrentExerciseName.text = state.currentExerciseName

                            timerProgressBar.max = state.currentExercise.duration.toInt()
                            bpmInput.apply {
                                text =
                                    Editable.Factory.getInstance().newEditable(state.newExerciseBpm)
                                hint = state.currentExerciseBpmRecord
                                isEnabled = true
                                setSelection(text.length.coerceAtLeast(0))
                            }
                        }
                    }
                    SessionState.Loading -> {
                        /* no-op */
                    }
                    SessionState.EmptyRoutine -> {
                        binding.apply {
                            sessionProgressBar.visibility = View.GONE
                            previousExerciseButton.visibility = View.GONE
                            nextExerciseButton.visibility = View.GONE
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

    private fun confirmFinishSession() {
        imm.hideKeyboard(requireView().windowToken)
        findNavController().safeNavigate(
            SessionFragmentDirections.actionSessionFragmentToConfirmationDialog(
                R.string.session_finish_dialog_title,
                R.string.session_finish_dialog_message,
                R.string.session_menu_finish_text,
                FINISH_SESSION
            )
        )
    }

    private fun gotToSummary() {
        requireContext().stopService(Intent(requireContext(), TimerService::class.java))
        imm.hideKeyboard(requireView().windowToken)
        findNavController().popBackStack(R.id.sessionFragment, false)
        findNavController().navigate(
            SessionFragmentDirections.actionSessionFragmentToSummaryFragment()
        )
    }
}