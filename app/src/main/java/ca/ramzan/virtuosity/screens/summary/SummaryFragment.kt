package ca.ramzan.virtuosity.screens.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.hideBottomNavBar
import ca.ramzan.virtuosity.databinding.FragmentSummaryBinding
import ca.ramzan.virtuosity.screens.BaseFragment
import ca.ramzan.virtuosity.screens.history.HistoryInnerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SummaryFragment : BaseFragment<FragmentSummaryBinding>() {

    private val viewModel: SummaryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().hideBottomNavBar()

        mutableBinding = FragmentSummaryBinding.inflate(inflater)

        binding.summaryToolbar.setNavigationOnClickListener {
            goBack()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.summary.collect { summary ->
                binding.summaryView.apply {
                    historyTitle.text = summary.title
                    historyDate.text = summary.displayTime
                    if (summary.note == null) {
                        notesData.visibility = View.GONE
                        headerNotes.visibility = View.GONE
                    } else {
                        notesData.text = summary.note
                        headerNotes.visibility = View.VISIBLE
                        notesData.visibility = View.VISIBLE
                    }
                    historyData.adapter = HistoryInnerAdapter(
                        summary.exercises,
                        summary.bpms,
                        summary.improvements
                    )
                    historyDeleteBtn.visibility = View.GONE
                }
            }
        }
        return binding.root
    }

    private fun goBack() {
        findNavController().popBackStack(R.id.routineListFragment, false)
    }
}