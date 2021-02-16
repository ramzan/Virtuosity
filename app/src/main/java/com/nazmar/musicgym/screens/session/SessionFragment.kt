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
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.R
import com.nazmar.musicgym.common.*
import com.nazmar.musicgym.databinding.FragmentSessionBinding
import com.nazmar.musicgym.screens.BaseFragment
import com.nazmar.musicgym.session.timer.Timer
import com.nazmar.musicgym.session.timer.TimerService
import com.nazmar.musicgym.session.timer.TimerState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SessionFragment : BaseFragment<FragmentSessionBinding>() {

    @Inject
    lateinit var factory: SessionViewModel.Factory

    private val viewModel: SessionViewModel by navGraphViewModels(R.id.sessionGraph) {
        SessionViewModel.provideFactory(factory, requireArguments().getLong("routineId"))
    }

    private lateinit var imm: InputMethodManager

    private lateinit var mTimer: Timer
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val timerService = service as TimerService.TimerBinder
            mTimer = timerService.getTimer()
            mBound = true

            mTimer.setUpTimer(viewModel.currentExercise.value)

            mTimer.timeString.observe(viewLifecycleOwner) {
                binding.timer.text = it
                binding.timerEditor.setText(it)
            }

            mTimer.timerStatus.observe(viewLifecycleOwner) {
                it?.let {
                    when (it) {
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

            viewModel.sessionName.observe(viewLifecycleOwner) {
                binding.sessionToolbar.title = it
                timerService.updateRoutineName(it)
            }

            viewModel.editorTime.observe(viewLifecycleOwner) {
                it?.let {
                    mTimer.updateTimeLeft(it)
                    viewModel.clearEditorTime()
                }
            }

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
                    mTimer.pauseTimer()
                }

                startTimerButton.setOnClickListener {
                    mTimer.startTimer()
                }

                restartTimerButton.setOnClickListener {
                    mTimer.restartTimer()
                }

                timerEditor.setOnClickListener {
                    showTimerEditor()
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
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
    }

    override fun onStop() {
        super.onStop()
        requireContext().unbindService(connection)
        mBound = false
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

        imm = requireActivity().getInputMethodManager()

        _binding = FragmentSessionBinding.inflate(inflater)

        viewModel.exercises.observe(viewLifecycleOwner) {
            if (viewModel.currentIndex.value == -1) {
                viewModel.nextExercise()
            }
        }

        binding.sessionToolbar.setNavigationOnClickListener {
            goBack()
        }

        SummaryExerciseAdapter().let { adapter ->
            adapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

            binding.summaryList.adapter = adapter

            viewModel.summaryList.observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
        }

        viewModel.currentExercise.observe(viewLifecycleOwner) {
            binding.apply {
                previousExerciseButton.isEnabled = viewModel.previousButtonEnabled
                setButtonVisibility()
                if (mBound) mTimer.setUpTimer(viewModel.currentExercise.value)

                if (it == null) {
                    if (mBound) mTimer.clearTimer()
                    summaryView.visibility = View.VISIBLE
                    exerciseView.visibility = View.GONE
                    bpmInput.isEnabled = false
                } else {
                    summaryView.visibility = View.GONE
                    exerciseView.visibility = View.VISIBLE
                    sessionCurrentExerciseName.text = viewModel.currentExerciseName
                    bpmInput.apply {
                        text = Editable.Factory.getInstance().newEditable(viewModel.newExerciseBpm)
                        hint = viewModel.currentExerciseBpmRecord
                        isEnabled = true
                        setSelection(text.length.coerceAtLeast(0))
                    }
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().showBottomNavBar()
    }

    private fun goBack() {
        requireContext().stopService(Intent(requireContext(), TimerService::class.java))
        imm.hideKeyboard(requireView().windowToken)
        findNavController().popBackStack(R.id.routineListFragment, false)
    }

    private fun setButtonVisibility() {
        when (viewModel.nextButtonEnabled) {
            true -> {
                binding.nextExerciseButton.visibility = View.VISIBLE
                binding.doneButton.visibility = View.GONE
            }
            else -> {
                binding.nextExerciseButton.visibility = View.GONE
                binding.doneButton.visibility = View.VISIBLE
            }
        }
    }

    private fun showTimerEditor() {
        findNavController().safeNavigate(
            SessionFragmentDirections.actionSessionFragmentToTimerEditorDialogFragment(
                requireArguments().getLong("routineId"),
                mTimer.timeLeft.value ?: 0L
            )
        )
    }
}