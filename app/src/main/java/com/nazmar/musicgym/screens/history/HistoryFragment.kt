package com.nazmar.musicgym.screens.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.R
import com.nazmar.musicgym.common.CONFIRMATION_RESULT
import com.nazmar.musicgym.common.POSITIVE_RESULT
import com.nazmar.musicgym.common.safeNavigate
import com.nazmar.musicgym.common.showBottomNavBar
import com.nazmar.musicgym.databinding.FragmentHistoryBinding
import com.nazmar.musicgym.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater)

        val adapter = SessionHistoryDisplayAdapter(::deleteSessionHistory)

        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.historyList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.history.collectLatest {
                adapter.submitData(it)
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
            HistoryFragmentDirections.actionHistoryFragmentToConfirmationDialog(R.string.delete_history_dialog_message)
        )
    }
}