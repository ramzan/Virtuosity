package com.nazmar.musicgym.practice.session

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.nazmar.musicgym.R
import com.nazmar.musicgym.databinding.FragmentSessionBinding
import com.nazmar.musicgym.hideBottomNavBar
import com.nazmar.musicgym.hideKeyboard
import com.nazmar.musicgym.showBottomNavBar

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

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().hideBottomNavBar()

        imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        _binding = FragmentSessionBinding.inflate(inflater)

        viewModel.exercises.observe(viewLifecycleOwner) {
            if (!viewModel.exercisesLoaded) {
                viewModel.createBpmList()
                viewModel.nextExercise()
            }
        }

        viewModel.currentIndex.observe(viewLifecycleOwner) {
            if (it > -1) {
                binding.sessionCurrentExerciseName.text = viewModel.getCurrentExerciseName()
                binding.bpmInput.hint = viewModel.getCurrentExerciseBpmRecord()
                binding.bpmInput.text = Editable.Factory.getInstance().newEditable(viewModel.getNewExerciseBpm())
                binding.previousExerciseButton.isEnabled = viewModel.previousButtonEnabled()
                setButtonVisibility()
                viewModel.setUpTimer()
            }
        }

        viewModel.timeString.observe(viewLifecycleOwner) {
            binding.timer.text = it
        }

        viewModel.timerStatus.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    TimerState.RUNNING -> {
                        binding.pauseTimerButton.visibility = View.VISIBLE
                        binding.startTimerButton.visibility = View.GONE
                    }
                    TimerState.PAUSED -> {
                        binding.pauseTimerButton.visibility = View.GONE
                        binding.startTimerButton.visibility = View.VISIBLE
                    }
                    TimerState.FINISHED -> {
                        Toast.makeText(requireContext(), "Time up!", Toast.LENGTH_SHORT).show()
                        viewModel.onAlarmRung()
                    }
                    TimerState.COMPLETED -> {
                        binding.pauseTimerButton.visibility = View.GONE
                        binding.startTimerButton.visibility = View.VISIBLE
                    }
                    TimerState.STOPPED -> {
                    }
                }
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
                viewModel.pauseTimer()
            }

            startTimerButton.setOnClickListener {
                viewModel.startTimer()
            }
            restartTimerButton.setOnClickListener {
                viewModel.restartTimer()
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
        viewModel.clearTimer()
        imm.hideKeyboard(requireView().windowToken)
        requireActivity().onBackPressed()
        requireActivity().showBottomNavBar()
    }

    private fun setButtonVisibility() {
        when (viewModel.nextButtonEnabled()) {
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
}