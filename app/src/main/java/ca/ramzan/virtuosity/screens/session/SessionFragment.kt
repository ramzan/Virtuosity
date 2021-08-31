package ca.ramzan.virtuosity.screens.session

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
import ca.ramzan.virtuosity.session.timer.TimerBinder
import ca.ramzan.virtuosity.session.timer.TimerService
import ca.ramzan.virtuosity.session.timer.TimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject


@AndroidEntryPoint
class SessionFragment : BaseFragment<FragmentSessionBinding>() {

    @Inject
    lateinit var imm: InputMethodManager

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var factory: SessionViewModel.Factory

    private val viewModel: SessionViewModel by viewModels {
        SessionViewModel.provideFactory(factory, requireArguments().getLong("routineId"))
    }

    private lateinit var timer: Timer
    private var bound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val timerService = service as TimerBinder
            timer = timerService.getTimer()
            bound = true

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                timer.timeString.collect { string ->
                    binding.timerDisplay.text = string
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
                    binding.apply {
                        when (status) {
                            TimerState.RUNNING -> {
                                pausePlayButton.setImageResource(R.drawable.ic_baseline_pause_48)
                                pausePlayButton.setOnClickListener {
                                    if (bound) this@SessionFragment.timer.pauseTimer()
                                }
                                timerEditor.visibility = View.INVISIBLE
                                timerDisplay.visibility = View.VISIBLE
                            }
                            TimerState.PAUSED, TimerState.STOPPED -> {
                                pausePlayButton.setImageResource(R.drawable.ic_baseline_play_arrow_48)
                                pausePlayButton.setOnClickListener {
                                    if (bound) this@SessionFragment.timer.startTimer()
                                }
                                timerEditor.visibility = View.VISIBLE
                                timerDisplay.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.state.collect { state ->
                    if (state is SessionState.PracticeScreen) {
                        timerService.updateRoutineName(state.sessionName)
                        timer.setUpTimer(
                            state.currentExercise.order,
                            state.currentExercise.name,
                            state.currentExercise.duration
                        )
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
            if (isOreoOrAbove()) requireContext().startForegroundService(intent)
            else requireContext().startService(intent)
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        setFragmentResultListener(DURATION_PICKER_RESULT) { _, bundle ->
            bundle.getLong(DURATION_VALUE).run {
                timer.updateTimeLeft(this)
                val newTime = this.toInt()
                binding.timerProgressBar.max = newTime
                binding.timerProgressBar.setProgressCompat(newTime, true)
                requireArguments().putInt(DURATION_VALUE, newTime)
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
        if (prefs.getBoolean(getString(R.string.key_session_stay_awake), false)) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        requireActivity().hideBottomNavBar()
        setUpBinding(FragmentSessionBinding.inflate(inflater))

        binding.apply {
            nextExerciseButton.setOnClickListener {
                requireArguments().remove(DURATION_VALUE)
                viewModel.nextExercise()
            }

            previousExerciseButton.setOnClickListener {
                requireArguments().remove(DURATION_VALUE)
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

            timerEditor.setOnClickListener { showTimerEditor() }

            sessionToolbar.setNavigationOnClickListener { goBack() }

            notesInput.doOnTextChanged { text, _, _, _ ->
                text?.let {
                    viewModel.updateNote(it.toString())
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is SessionState.PracticeScreen -> {
                        binding.apply {
                            sessionToolbar.title = state.sessionName

                            previousExerciseButton.isEnabled = state.previousButtonEnabled
                            nextExerciseButton.isEnabled = state.nextButtonEnabled

                            sessionCurrentExerciseName.text = getString(
                                R.string.session_current_exercise,
                                state.currentIndex + 1,
                                state.currentExerciseName
                            )

                            timerProgressBar.max =
                                requireArguments().getInt(
                                    DURATION_VALUE,
                                    state.currentExercise.duration.toInt()
                                )

                            bpmInput.apply {
                                setText(state.newExerciseBpm)
                                hint = state.currentExerciseBpmRecord
                                isEnabled = true
                                text?.length?.let { setSelection(it.coerceAtLeast(0)) }
                            }

                            if (!requireArguments().getBoolean(FIRST_RUN_KEY)) {
                                notesInput.setText(state.sessionNote)
                                requireArguments().putBoolean(FIRST_RUN_KEY, true)
                            }

                            sessionProgressBar.visibility = View.GONE
                            practiceView.visibility = View.VISIBLE

                        }
                    }
                    SessionState.Loading -> {
                        /* no-op */
                    }
                    SessionState.EmptyRoutine -> {
                        binding.apply {
                            sessionProgressBar.visibility = View.GONE
                            practiceView.visibility = View.GONE
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
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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