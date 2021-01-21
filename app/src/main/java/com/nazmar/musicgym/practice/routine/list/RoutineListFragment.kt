package com.nazmar.musicgym.practice.routine.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.databinding.FragmentRoutineListBinding
import com.nazmar.musicgym.db.Routine

class RoutineListFragment : Fragment() {

    private var _binding: FragmentRoutineListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RoutineListViewModel by viewModels {
        RoutineListViewModelFactory(requireNotNull(this.activity).application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentRoutineListBinding.inflate(inflater)

        RoutineAdapter(
            object : RoutineAdapter.OnClickListener {
                override fun onEdit(routine: Routine) = showRoutineEditor(routine.id)

                override fun onStart(routine: Routine) = startRoutine(routine.id)
            }
        ).run {
            this.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

            binding.routineList.adapter = this

            viewModel.routines.observe(viewLifecycleOwner, {
                this.submitList(it)
            })
        }

        binding.fab.setOnClickListener {
            showRoutineEditor(0)
        }

        return binding.root
    }

    private fun showRoutineEditor(id: Long) {
        val action = RoutineListFragmentDirections.actionPracticeFragmentToRoutineEditor(id)
        findNavController().navigate(action)
    }

    private fun startRoutine(id: Long) {
        val action = RoutineListFragmentDirections.actionPracticeFragmentToSessionFragment(id)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}