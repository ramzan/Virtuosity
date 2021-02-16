package com.nazmar.musicgym.screens.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.common.safeNavigate
import com.nazmar.musicgym.databinding.FragmentHistoryBinding
import com.nazmar.musicgym.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>() {

    private val viewModel: HistoryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater)

        SessionHistoryDisplayAdapter(::deleteSessionHistory).run {
            this.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

            binding.historyList.adapter = this

            viewModel.history.observe(viewLifecycleOwner, {
                this.submitList(it)
            })
        }

        return binding.root
    }

    private fun deleteSessionHistory(id: Long) {
        findNavController().safeNavigate(
            HistoryFragmentDirections.actionHistoryFragmentToDeleteHistoryDialogFragment(id)
        )
    }
}