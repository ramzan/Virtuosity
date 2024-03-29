package ca.ramzan.virtuosity.screens.exercise_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.ramzan.virtuosity.exercises.ExerciseLatestBpm
import ca.ramzan.virtuosity.exercises.ExerciseListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExerciseListViewModel @Inject constructor(private val useCase: ExerciseListUseCase) :
    ViewModel() {

    private var query = ""

    val state = MutableStateFlow<ExerciseListState>(ExerciseListState.Loading)

    init {
        viewModelScope.launch {
            useCase.getAllExerciseMaxBPMs().collect { list ->
                state.emit(ExerciseListState.Loaded(list, query))
            }
        }
    }

    fun setNameQuery(name: String) {
        query = name.lowercase(Locale.ROOT).trim()
        viewModelScope.launch {
            (state.value as? ExerciseListState.Loaded)?.run {
                state.emit(copy(query = query))
            }
        }
    }

    fun addExercise(name: String) = useCase.addExercise(name)

    // region editing routine ----------------------------------------------------------------------

    val selectedExercises: MutableList<ExerciseLatestBpm> = mutableListOf()
    val numExercisesSelected = MutableStateFlow(0)

    fun toggleSelected(exercise: ExerciseLatestBpm) {
        if (selectedExercises.contains(exercise)) selectedExercises.remove(exercise)
        else selectedExercises.add(exercise)
        viewModelScope.launch {
            numExercisesSelected.emit(selectedExercises.size)
        }
    }
    // endregion editing routine -------------------------------------------------------------------
}

sealed class ExerciseListState {
    object Loading : ExerciseListState()

    data class Loaded(
        private val allExercises: List<ExerciseLatestBpm>,
        private val query: String
    ) :
        ExerciseListState() {

        val filteredExercises = allExercises.filter { exercise ->
            exercise.name.lowercase(Locale.ROOT).contains(query)
        }
    }
}