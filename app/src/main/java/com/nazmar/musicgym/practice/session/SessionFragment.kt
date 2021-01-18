package com.nazmar.musicgym.practice.session

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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.nazmar.musicgym.*
import com.nazmar.musicgym.databinding.FragmentSessionBinding


class SessionFragment : Fragment() {

    private var _binding: FragmentSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionViewModel by navGraphViewModels(R.id.sessionGraph) {
        SessionViewModelFactory(
                arguments?.get(
                        "routineId"
                ) as Long, requireNotNull(this.activity).application
        )
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
                        TimerState.PAUSED -> {
                            binding.pauseTimerButton.visibility = View.GONE
                            binding.startTimerButton.visibility = View.VISIBLE

                            binding.timerEditor.visibility = View.VISIBLE
                            binding.timer.visibility = View.GONE
                        }
                        TimerState.STOPPED -> {
                            binding.pauseTimerButton.visibility = View.GONE
                            binding.startTimerButton.visibility = View.VISIBLE

                            binding.timerEditor.visibility = View.VISIBLE
                            binding.timer.visibility = View.GONE
                        }
                    }
                }
            }

            viewModel.session.observe(viewLifecycleOwner) {
                it?.name?.let { routineName ->
                    binding.sessionToolbar.title = routineName
                    timerService.updateRoutineName(routineName)
                }
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
                        viewModel.updateBpm(it.toString())
                    }
                }

                doneButton.setOnClickListener {
                    viewModel.saveSession()
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
                viewModel.createBpmList()
                viewModel.nextExercise()
            }
        }

        binding.sessionToolbar.setNavigationOnClickListener {
            goBack()
        }

        viewModel.currentExercise.observe(viewLifecycleOwner) {
            binding.previousExerciseButton.isEnabled = viewModel.previousButtonEnabled
            setButtonVisibility()
            if (mBound) mTimer.setUpTimer(viewModel.currentExercise.value)

            if (it == null) {
                if (mBound) mTimer.clearTimer()
                binding.summaryView.visibility = View.VISIBLE
                binding.exerciseView.visibility = View.GONE
            } else {
                binding.summaryView.visibility = View.GONE
                binding.exerciseView.visibility = View.VISIBLE
                binding.sessionCurrentExerciseName.text = viewModel.currentExerciseName
                binding.bpmInput.hint = viewModel.currentExerciseBpmRecord
                binding.bpmInput.text = Editable.Factory.getInstance().newEditable(viewModel.newExerciseBpm)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().showBottomNavBar()
        _binding = null
    }

    private fun goBack() {
        requireContext().stopService(Intent(requireContext(), TimerService::class.java))
        imm.hideKeyboard(requireView().windowToken)
        findNavController().popBackStack()
        requireActivity().showBottomNavBar()
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
        val action =
                SessionFragmentDirections.actionSessionFragmentToTimerEditorDialogFragment(
                        arguments?.get(
                                "routineId"
                        ) as Long,
                        mTimer.timeLeft.value ?: 0L
                )
        findNavController().navigate(action)
    }
}