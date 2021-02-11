package com.nazmar.musicgym.exercises.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.nazmar.musicgym.BaseFragment
import com.nazmar.musicgym.R
import com.nazmar.musicgym.databinding.FragmentExerciseDetailBinding
import com.nazmar.musicgym.hideBottomNavBar
import com.nazmar.musicgym.showBottomNavBar
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