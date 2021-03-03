package com.nazmar.musicgym.screens.exercisedetail

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.nazmar.musicgym.R
import com.nazmar.musicgym.common.*
import com.nazmar.musicgym.databinding.FragmentExerciseDetailBinding
import com.nazmar.musicgym.exercises.ExerciseDetailUseCase
import com.nazmar.musicgym.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class ExerciseDetailFragment : BaseFragment<FragmentExerciseDetailBinding>() {

    @Inject
    lateinit var factory: ExerciseDetailViewModel.Factory

    private val viewModel: ExerciseDetailViewModel by viewModels {
        ExerciseDetailViewModel.provideFactory(factory, requireArguments().getLong("exerciseId"))
    }

    private val dayMonthFormatter = object : ValueFormatter() {
        private val format = DateTimeFormatter.ofPattern("dd MMM")

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

    private val yAxisFormatter = object : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?) = value.toInt().toString()
    }

    override fun onStart() {
        super.onStart()
        setFragmentResultListener(CONFIRMATION_RESULT) { _, bundle ->
            if (bundle.getBoolean(POSITIVE_RESULT)) {
                viewModel.deleteExercise()
                goBack()
            }
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


        binding.historyGraph.apply {
            description = null
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisLeft.valueFormatter = yAxisFormatter

            viewModel.history.observe(viewLifecycleOwner) { state ->
                when (state) {
                    ExerciseDetailUseCase.GraphState.Loading -> {
                        binding.loadingIndicator.visibility = View.VISIBLE
                        binding.historyGraph.visibility = View.GONE
                        binding.noDataMessage.visibility = View.GONE
                    }
                    ExerciseDetailUseCase.GraphState.NoData -> {
                        binding.loadingIndicator.visibility = View.GONE
                        binding.historyGraph.visibility = View.GONE
                        binding.noDataMessage.visibility = View.VISIBLE
                    }
                    is ExerciseDetailUseCase.GraphState.Loaded -> {
                        binding.loadingIndicator.visibility = View.GONE
                        binding.historyGraph.visibility = View.VISIBLE
                        binding.noDataMessage.visibility = View.GONE
                        val dataSet = LineDataSet(state.data, "BPM")
                        data = LineData(dataSet)
                        axisLeft.axisMaximum = state.maxBpm * 1.05f
                        binding.apply {
                            bpmSlowest.text = getString(
                                R.string.history_stats_slowest_message,
                                state.minBpm.toLong()
                            )
                            bpmFastest.text = getString(
                                R.string.history_stats_fastest_message,
                                state.maxBpm.toLong()
                            )
                            bpmProgress.text = getString(
                                if (state.periodImprovement < 0) {
                                    R.string.history_stats_progress_negative_message
                                } else R.string.history_stats_progress_message,
                                state.periodImprovement
                            )
                        }
                        invalidate()
                    }
                }
            }

            // create marker to display box when values are selected
            val mv = MyMarkerView(requireContext(), R.layout.custom_marker_view)
            mv.chartView = this
            marker = mv
        }


        binding.historyRangerSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    binding.historyGraph.xAxis.valueFormatter = when (position) {
                        0, 1, 2 -> dayMonthFormatter
                        3, 4 -> monthYearFormatter
                        else -> throw Exception("Illegal spinner position: $position")
                    }
                    viewModel.getHistory(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    /* no-op */
                }
            }

        return binding.root
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

@SuppressLint("ViewConstructor")
class MyMarkerView(context: Context, @LayoutRes layoutResource: Int) :
    MarkerView(context, layoutResource) {
    private val bpmText: TextView = findViewById(R.id.bpm_text)
    private val dateText: TextView = findViewById(R.id.date_text)

    override fun refreshContent(entry: Entry, highlight: Highlight) {
        val bpm = "${entry.y.toInt()} BPM"
        val date = "${Date(entry.x.toLong())}"
        bpmText.text = bpm
        dateText.text = date
        super.refreshContent(entry, highlight)
    }

    override fun getOffset() = MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
}