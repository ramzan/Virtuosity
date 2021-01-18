package com.nazmar.musicgym.exercises.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.R
import com.nazmar.musicgym.databinding.FragmentExerciseListBinding
import com.nazmar.musicgym.hideKeyboard
import com.nazmar.musicgym.showKeyboard


class ExerciseListFragment : Fragment() {

    private var _binding: FragmentExerciseListBinding? = null
    private val binding get() = _binding!!

    private lateinit var imm: InputMethodManager

    private val viewModel: ExerciseListViewModel by activityViewModels {
        ExerciseListViewModelFactory(
                requireNotNull(this.activity).application
        )
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseListBinding.inflate(inflater)

        ExerciseAdapter(ExerciseAdapter.OnClickListener {
            showExerciseView(it.id)
        }).run {
            this.stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

            binding.exerciseList.adapter = this

            viewModel.exercises.observe(viewLifecycleOwner, {
                this.submitList(it)
            })
        }

        binding.fab.setOnClickListener {
            showNewExerciseDialog()
        }

        binding.exercisesToolbar.menu.findItem(R.id.search).apply {
            (actionView as SearchView).apply {
                isIconified = false
                queryHint = getString(R.string.exercises_search_hint)
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(text: String?): Boolean {
                        return true
                    }

                    override fun onQueryTextChange(text: String?): Boolean {
                        viewModel.setNameQuery(text.toString())
                        return true
                    }
                })
            }

            imm = (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                    (actionView as SearchView).onActionViewExpanded()
                    imm.showKeyboard()
                    (actionView as SearchView).requestFocus()
                    return true
                }

                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                    viewModel.setNameQuery("")
                    (actionView as SearchView).setQuery("", false)
                    imm.hideKeyboard(requireView().windowToken)
                    return true
                }

            })
        }
        return binding.root
    }

    private fun showExerciseView(id: Long) {
        val action = ExerciseListFragmentDirections.actionExercisesFragmentToExercisesGraph(id)
        findNavController().navigate(action)
    }

    private fun showNewExerciseDialog() {
        val action = ExerciseListFragmentDirections.actionExerciseListFragmentToNewExerciseDialogFragment()
        findNavController().navigate(action)
    }

    override fun onStop() {
        viewModel.setNameQuery("")
        imm.hideKeyboard(requireView().windowToken)
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}