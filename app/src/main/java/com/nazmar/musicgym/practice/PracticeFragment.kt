package com.nazmar.musicgym.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.R
import com.nazmar.musicgym.databinding.FragmentPracticeBinding

class PracticeFragment : Fragment() {

    private var _binding: FragmentPracticeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PracticeViewModel by viewModels {
        PracticeViewModelFactory(requireNotNull(this.activity).application)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentPracticeBinding.inflate(inflater)

        val adapter = RoutineAdapter(RoutineAdapter.OnClickListener {
            showRoutineEditor(it.id)
        })


        adapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY


        binding.routineList.adapter = adapter

        viewModel.routines.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        binding.fab.setOnClickListener {
            showRoutineEditor(0)
        }

        return binding.root
    }

    private fun showRoutineEditor(id: Long) {
        val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        val action = PracticeFragmentDirections.actionPracticeFragmentToRoutineEditor(id)
        navController.navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}