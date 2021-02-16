package com.nazmar.musicgym.screens.exercisedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.nazmar.musicgym.R
import com.nazmar.musicgym.common.*
import com.nazmar.musicgym.databinding.FragmentExerciseDetailBinding
import com.nazmar.musicgym.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class ExerciseDetailFragment : BaseFragment<FragmentExerciseDetailBinding>() {

    @Inject
    lateinit var factory: ExerciseDetailViewModel.Factory

    private val viewModel: ExerciseDetailViewModel by viewModels {
        ExerciseDetailViewModel.provideFactory(factory, requireArguments().getLong("exerciseId"))
    }

    private val dayMonthFormatter = object : ValueFormatter() {
        private val format = DateTimeFormatter.ofPattern("dd MM")

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return Instant.ofEpochMilli(value.toLong()).run {
                format.format(this.atZone(ZoneId.systemDefault()))
            }
        }
    }

    private val monthYearFormatter = object : ValueFormatter() {
        private val format = DateTimeFormatter.ofPattern("MM-yyyy")

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return Instant.ofEpochMilli(value.toLong()).run {
                format.format(this.atZone(ZoneId.systemDefault()))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setFragmentResultListener(CONFIRMATION_RESULT) { _, bundle ->
            if (bundle.getBoolean(POSITIVE_RESULT)) viewModel.deleteExercise()
        }
        setFragmentResultListener(TEXT_INPUT_RESULT) { _, bundle ->
            bundle.getString(INPUT_TEXT)?.let { viewModel.renameExercise(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        requireActivity().hideBottomNavBar()

        _binding = FragmentExerciseDetailBinding.inflate(inflater)

        binding.editorToolbar.apply {
            setNavigationOnClickListener {
                goBack()
            }

            viewModel.exercise.observe(viewLifecycleOwner) {
                title = it?.name ?: ""
                menu.getItem(0).isEnabled = it !== null
                menu.getItem(1).isEnabled = it !== null
                if (viewModel.exerciseDeleted) goBack()
            }

            // Rename button
            menu.getItem(0).setOnMenuItemClickListener {
                showRenameDialog()
                true
            }

            // Delete button
            menu.getItem(1).setOnMenuItemClickListener {
                showDeleteDialog()
                true
            }
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.history_range_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.historyRangerSpinner.adapter = adapter
        }

        binding.historyGraph.description = null
        // Y axis
        binding.historyGraph.axisRight.isEnabled = false
        binding.historyGraph.axisLeft.axisMinimum = 0f
        viewModel.graphMax.observe(viewLifecycleOwner) {
            binding.historyGraph.axisLeft.apply {
                axisMaximum = it * 1.1f
            }
        }

        viewModel.history.observe(viewLifecycleOwner) {
            binding.historyGraph.apply {
                val dataSet = LineDataSet(it, "BPM")
                data = LineData(dataSet)
                invalidate()
            }
        }

        binding.historyRangerSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        0 -> setGraphWeek()
                        1 -> setGraphMonth()
                        2 -> setGraphQuarter()
                        3 -> setGraphYear()
                        4 -> setGraphAll()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }

        return binding.root
    }

    private fun setGraphWeek() {
        viewModel.getWeekHistory()
        binding.historyGraph.xAxis.valueFormatter = dayMonthFormatter
    }

    private fun setGraphMonth() {
        viewModel.getMonthHistory()
        binding.historyGraph.xAxis.valueFormatter = dayMonthFormatter
    }

    private fun setGraphQuarter() {
        viewModel.getQuarterHistory()
        binding.historyGraph.xAxis.valueFormatter = dayMonthFormatter
    }

    private fun setGraphYear() {
        viewModel.getYearHistory()
        binding.historyGraph.xAxis.valueFormatter = monthYearFormatter
    }

    private fun setGraphAll() {
        viewModel.getAllHistory()
        binding.historyGraph.xAxis.valueFormatter = monthYearFormatter
    }

    private fun showDeleteDialog() {
        findNavController().safeNavigate(
            ExerciseDetailFragmentDirections.actionExerciseDetailFragmentToConfirmationDialog(
                R.string.delete_exercise_dialog_message
            )
        )
    }

    private fun showRenameDialog() {
        findNavController().safeNavigate(
            ExerciseDetailFragmentDirections.actionExerciseDetailFragmentToTextInputDialog(
                R.string.rename,
                viewModel.exercise.value?.name ?: ""
            )
        )
    }

    private fun goBack() {
        findNavController().popBackStack(R.id.exerciseListFragment, false)
    }
}