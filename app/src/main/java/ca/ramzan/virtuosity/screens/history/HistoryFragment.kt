package ca.ramzan.virtuosity.screens.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.CONFIRMATION_RESULT
import ca.ramzan.virtuosity.common.POSITIVE_RESULT
import ca.ramzan.virtuosity.common.safeNavigate
import ca.ramzan.virtuosity.common.showBottomNavBar
import ca.ramzan.virtuosity.databinding.FragmentHistoryBinding
import ca.ramzan.virtuosity.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>() {

    private val viewModel: HistoryViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        requireActivity().showBottomNavBar()
        setFragmentResultListener(CONFIRMATION_RESULT) { _, bundle ->
            if (bundle.getBoolean(POSITIVE_RESULT)) viewModel.deleteHistoryItem()
        }
    }

    private var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater)

        val adapter = HistoryOuterAdapter(::deleteSessionHistory)

        binding.historyList.adapter = adapter

        job = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.history.collectLatest {
                adapter.submitData(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            adapter.loadStateFlow.collectLatest { state ->
                when (state.refresh) {
                    is LoadState.Loading -> {
                        binding.historyProgressBar.visibility = View.VISIBLE
                        binding.historyList.visibility = View.GONE
                    }
                    else -> {
                        binding.historyProgressBar.visibility = View.GONE
                        binding.historyList.visibility = View.VISIBLE
                    }
                }
            }
        }

        return binding.root
    }

    override fun onStop() {
        job?.cancel()
        super.onStop()
    }

    override fun onDestroyView() {
        binding.historyList.adapter = null
        super.onDestroyView()
    }

    private fun deleteSessionHistory(id: Long) {
        viewModel.pendingDeleteId = id
        findNavController().safeNavigate(
            HistoryFragmentDirections.actionHistoryFragmentToConfirmationDialog(R.string.delete_history_dialog_message)
        )
    }
}