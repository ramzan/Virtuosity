package com.nazmar.musicgym.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by activityViewModels {
        HistoryViewModelFactory()
    }

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
        findNavController().navigate(
            HistoryFragmentDirections.actionHistoryFragmentToDeleteHistoryDialogFragment(id)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}