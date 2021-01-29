package com.nazmar.musicgym.exercises.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.nazmar.musicgym.R
import com.nazmar.musicgym.databinding.FragmentExerciseDetailBinding
import com.nazmar.musicgym.hideBottomNavBar
import com.nazmar.musicgym.showBottomNavBar


class ExerciseDetailFragment : Fragment() {

    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExerciseDetailViewModel by navGraphViewModels(R.id.exercisesGraph) {
        ExerciseDetailViewModelFactory(requireArguments().getLong("exerciseId"))
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
        _binding = null
    }
}