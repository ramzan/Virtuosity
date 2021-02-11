package com.nazmar.musicgym.screens.exercisedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.nazmar.musicgym.R
import com.nazmar.musicgym.common.hideBottomNavBar
import com.nazmar.musicgym.common.showBottomNavBar
import com.nazmar.musicgym.databinding.FragmentExerciseDetailBinding
import com.nazmar.musicgym.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExerciseDetailFragment : BaseFragment<FragmentExerciseDetailBinding>() {

    @Inject
    lateinit var factory: ExerciseDetailViewModel.Factory

    private val viewModel: ExerciseDetailViewModel by navGraphViewModels(R.id.exercisesGraph) {
        ExerciseDetailViewModel.provideFactory(factory, requireArguments().getLong("exerciseId"))
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

            viewModel.exercise.observe(viewLifecycleOwner) {
                title = it?.name ?: ""
                menu.getItem(0).isEnabled = it !== null
                menu.getItem(1).isEnabled = it !== null
                if (viewModel.exerciseDeleted) goBack()
            }

            // Rename button
            menu.getItem(0).setOnMenuItemClickListener {
                showRenameDialog()
                true
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

        binding.historyRangerSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        0 -> viewModel.getWeekHistory()
                        1 -> viewModel.getMonthHistory()
                        2 -> viewModel.getQuarterHistory()
                        3 -> viewModel.getYearHistory()
                        4 -> viewModel.getAllHistory()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }

        return binding.root
    }

    private fun showDeleteDialog() {
        findNavController().navigate(
            ExerciseDetailFragmentDirections.actionExerciseDetailFragmentToDeleteDialogFragment(
                requireArguments().getLong("exerciseId")
            )
        )
    }

    private fun showRenameDialog() {
        findNavController().navigate(
            ExerciseDetailFragmentDirections.actionExerciseDetailFragmentToRenameDialogFragment(
                requireArguments().getLong("exerciseId")
            )
        )
    }

    private fun goBack() {
        findNavController().popBackStack(R.id.exerciseListFragment, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().showBottomNavBar()
    }
}