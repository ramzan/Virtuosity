package ca.ramzan.virtuosity.exercises

import ca.ramzan.virtuosity.common.room.ExerciseListDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExerciseListUseCase @Inject constructor(private val dao: ExerciseListDao) {

    fun getAllExerciseMaxBPMs() = dao.getAllExerciseLatestBpms()

    fun addExercise(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(Exercise(name))
        }
    }
}