package ca.ramzan.virtuosity.exercises

import ca.ramzan.virtuosity.common.room.ExerciseDetailDao
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExerciseDetailUseCase @Inject constructor(private val dao: ExerciseDetailDao) {

    fun getExercise(id: Long) = dao.getExercise(id)

    private val _graphState = MutableStateFlow<GraphState>(GraphState.Loading)

    val graphState: StateFlow<GraphState> get() = _graphState

    sealed class GraphState {
        object Loading : GraphState()
        object NoData : GraphState()
        data class Loaded(
            val maxBpm: Float,
            val minBpm: Float,
            val avgBpm: Int,
            val periodImprovement: Int,
            val dataSet: LineDataSet
        ) : GraphState()
    }

    suspend fun getExerciseHistorySince(exerciseId: Long, startTime: Long) {
        _graphState.emit(GraphState.Loading)
        val history = dao.getExerciseHistorySince(exerciseId, startTime).map {
            Entry(it.time.toFloat(), it.bpm.toFloat())
        }
        _graphState.emit(
            if (history.isEmpty()) GraphState.NoData
            else GraphState.Loaded(
                history.maxOf { it.y },
                history.minOf { it.y },
                history.map { it.y }.average().toInt(),
                (history.last().y - history.first().y).toInt(),
                LineDataSet(history, null)
            )
        )
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