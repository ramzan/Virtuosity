package ca.ramzan.virtuosity.screens.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import ca.ramzan.virtuosity.R
import ca.ramzan.virtuosity.common.CONFIRMATION_RESULT
import ca.ramzan.virtuosity.common.DELETE_HISTORY
import ca.ramzan.virtuosity.common.safeNavigate
import ca.ramzan.virtuosity.common.showBottomNavBar
import ca.ramzan.virtuosity.databinding.FragmentHistoryBinding
import ca.ramzan.virtuosity.screens.BaseFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>() {

    private val viewModel: HistoryViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        requireActivity().showBottomNavBar()
        setFragmentResultListener(CONFIRMATION_RESULT) { _, bundle ->
            if (bundle.getBoolean(DELETE_HISTORY)) {
                viewModel.deleteHistoryItem()
                Snackbar.make(
                    requireActivity().findViewById(R.id.nav_view),
                    getString(R.string.history_deleted_message),
                    Snackbar.LENGTH_SHORT
                )
                    .setAnchorView(requireActivity().findViewById(R.id.nav_view))
                    .show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mutableBinding = FragmentHistoryBinding.inflate(inflater)

        val adapter = HistoryOuterAdapter(::deleteSessionHistory)

        binding.historyList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.history.collectLatest {
                adapter.submitData(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            adapter.loadStateFlow.collectLatest { state ->
                when (state.refresh) {
                    is LoadState.Loading -> {
                        if (adapter.itemCount == 0) {
                            binding.historyProgressBar.visibility = View.VISIBLE
                            binding.historyList.visibility = View.GONE
                            binding.noHistoryMessage.visibility = View.GONE
                        }
                    }
                    else -> {
                        if (adapter.itemCount == 0) {
                            binding.historyProgressBar.visibility = View.GONE
                            binding.historyList.visibility = View.GONE
                            binding.noHistoryMessage.visibility = View.VISIBLE
                        } else {
                            binding.historyProgressBar.visibility = View.GONE
                            binding.historyList.visibility = View.VISIBLE
                            binding.noHistoryMessage.visibility = View.GONE
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        binding.historyList.adapter = null
        super.onDestroyView()
    }

    private fun deleteSessionHistory(id: Long) {
        viewModel.pendingDeleteId = id
        findNavController().safeNavigate(
            HistoryFragmentDirections.actionHistoryFragmentToConfirmationDialog(
                R.string.delete_history_dialog_title,
                R.string.message_action_cannot_be_undone,
                R.string.delete,
                DELETE_HISTORY
            )
        )
    }
}