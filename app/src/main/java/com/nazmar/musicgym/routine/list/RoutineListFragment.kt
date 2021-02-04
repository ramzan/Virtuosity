package com.nazmar.musicgym.routine.list

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.SAVED_SESSION_ID
import com.nazmar.musicgym.SAVED_SESSION_NAME
import com.nazmar.musicgym.SAVED_SESSION_TIME
import com.nazmar.musicgym.databinding.FragmentRoutineListBinding
import com.nazmar.musicgym.db.Routine
import java.util.*

class RoutineListFragment : Fragment() {

    private var _binding: FragmentRoutineListBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: SharedPreferences

    private val viewModel: RoutineListViewModel by viewModels {
        RoutineListViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentRoutineListBinding.inflate(inflater)

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        viewModel.sessionSaved.observe(viewLifecycleOwner) {
            binding.apply {
                if (it) {
                    savedSessionCard.visibility = View.VISIBLE
                    savedSessionName.text = prefs.getString(SAVED_SESSION_NAME, "")
                    savedSessionDate.text = Date(
                        prefs.getLong(
                            SAVED_SESSION_TIME,
                            System.currentTimeMillis()
                        )
                    ).toString()
                    resumeSessionBtn.isEnabled = true
                } else {
                    savedSessionCard.visibility = View.GONE
                    savedSessionName.text = ""
                    savedSessionDate.text = ""
                    resumeSessionBtn.isEnabled = false
                }
            }
        }

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

        binding.resumeSessionBtn.setOnClickListener {
            with(prefs.getLong(SAVED_SESSION_ID, 0)) {
                if (this > 0) {
                    findNavController().navigate(
                        RoutineListFragmentDirections.actionRoutineListFragmentToSessionGraph(
                            this
                        )
                    )
                }
            }

        }

        return binding.root
    }

    private fun showRoutineEditor(id: Long) {
        findNavController().navigate(
            RoutineListFragmentDirections.actionRoutineListFragmentToRoutineEditorGraph(id)
        )
    }

    private fun startRoutine(id: Long) {
        findNavController().navigate(
            if (prefs.contains(SAVED_SESSION_NAME)) {
                RoutineListFragmentDirections.actionRoutineListFragmentToRestartSessionDialogFragment(
                    id
                )
            } else {
                RoutineListFragmentDirections.actionRoutineListFragmentToSessionGraph(id)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}