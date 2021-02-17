package com.nazmar.musicgym.exercises

import com.github.mikephil.charting.data.Entry
import com.nazmar.musicgym.common.room.ExerciseDetailDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExerciseDetailUseCase @Inject constructor(private val dao: ExerciseDetailDao) {

    fun getExercise(id: Long) = dao.getExercise(id)

    private val _graphState = MutableStateFlow<GraphState>(GraphState.Loading)

    val graphState get() = _graphState

    sealed class GraphState {
        object Loading : GraphState()
        data class Loaded(
            val maxBpm: Float,
            val data: List<Entry>
        ) : GraphState()
    }

    suspend fun getExerciseHistorySince(exerciseId: Long, startTime: Long) {
        _graphState.emit(GraphState.Loading)
        val history = dao.getExerciseHistorySince(exerciseId, startTime).map {
            Entry(it.time.toFloat(), it.bpm.toFloat())
        }
        _graphState.emit(GraphState.Loaded((history.maxOfOrNull { it.y } ?: 0f) * 1.05f, history))
    }

    fun renameExercise(exercise: Exercise, newName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.update(exercise.copy(name = newName))
        }
    }

    fun deleteExercise(exercise: Exercise) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(exercise)
        }
    }
}