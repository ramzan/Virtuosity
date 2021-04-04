package ca.ramzan.virtuosity.screens.exercisedetail

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.*
import ca.ramzan.virtuosity.databinding.FragmentExerciseDetailBinding
import ca.ramzan.virtuosity.exercises.ExerciseDetailUseCase
import ca.ramzan.virtuosity.screens.BaseFragment
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
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

    private val bpmFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
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

            lifecycleScope.launchWhenStarted {
                viewModel.exercise.collect { state ->
                    if (state is ExerciseDetailState.Loaded) {
                        title = state.exercise.name
                        menu.getItem(0).isEnabled = true
                        menu.getItem(1).isEnabled = true

                        // Rename button
                        menu.getItem(0).setOnMenuItemClickListener {
                            showRenameDialog(state.exercise.name)
                            true
                        }
                    } else if (state is ExerciseDetailState.Deleted) goBack()
                }
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

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.history.collect { state ->
                    when (state) {
                        ExerciseDetailUseCase.GraphState.Loading -> {
                            binding.apply {
                                loadingIndicator.visibility = View.VISIBLE
                                historyGraphCard.visibility = View.GONE
                                statsCard.visibility = View.GONE
                                noDataMessage.visibility = View.GONE
                            }
                        }
                        ExerciseDetailUseCase.GraphState.NoData -> {
                            binding.apply {
                                loadingIndicator.visibility = View.GONE
                                historyGraphCard.visibility = View.GONE
                                statsCard.visibility = View.GONE
                                noDataMessage.visibility = View.VISIBLE
                            }
                        }
                        is ExerciseDetailUseCase.GraphState.Loaded -> {
                            binding.apply {
                                loadingIndicator.visibility = View.GONE
                                historyGraphCard.visibility = View.VISIBLE
                                statsCard.visibility = View.VISIBLE
                                noDataMessage.visibility = View.GONE
                                val dataSet = LineDataSet(state.data, "BPM")
                                data = LineData(dataSet)
                                data.setValueFormatter(bpmFormatter)
                                axisLeft.axisMaximum = state.maxBpm * 1.05f

                                bpmSlowest.text = getString(
                                    R.string.history_stats_slowest_message,
                                    state.minBpm.toInt()
                                )
                                bpmFastest.text = getString(
                                    R.string.history_stats_fastest_message,
                                    state.maxBpm.toInt()
                                )
                                bpmAverage.text = getString(
                                    R.string.history_stats_average_message,
                                    state.avgBpm
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

    private fun showRenameDialog(oldName: String) {
        findNavController().safeNavigate(
            ExerciseDetailFragmentDirections.actionExerciseDetailFragmentToTextInputDialog(
                R.string.rename,
                oldName
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
        val date =
            Instant.ofEpochMilli(entry.x.toLong()).atZone(ZoneId.systemDefault()).format(formatter)
        bpmText.text = bpm
        dateText.text = date
        super.refreshContent(entry, highlight)
    }

    override fun getOffset() = MPPointF((-(width / 2)).toFloat(), (-height).toFloat())

    companion object {
        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}