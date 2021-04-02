package ca.ramzan.virtuosity.common.room

import androidx.paging.PagingSource
import androidx.room.*
import ca.ramzan.virtuosity.exercises.Exercise
import ca.ramzan.virtuosity.exercises.ExerciseHistory
import ca.ramzan.virtuosity.exercises.ExerciseLatestBpm
import ca.ramzan.virtuosity.exercises.HistoryGraphDataPoint
import ca.ramzan.virtuosity.history.SessionHistoryEntity
import ca.ramzan.virtuosity.routine.Routine
import ca.ramzan.virtuosity.routine.RoutineDisplay
import ca.ramzan.virtuosity.routine.RoutineExercise
import ca.ramzan.virtuosity.routine.RoutineExerciseEntity
import ca.ramzan.virtuosity.session.SessionExercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface ExerciseListDao {
    @Insert
    suspend fun insert(exercise: Exercise)

    @Query(
        """
        SELECT id, name, bpm FROM exercise_table
        LEFT OUTER JOIN exercise_history_table ON exerciseId = id
        GROUP BY id
        ORDER BY name COLLATE NOCASE ASC, time DESC
        """
    )
    fun getAllExerciseLatestBpms(): Flow<List<ExerciseLatestBpm>>
}

@Dao
interface RoutineEditorDao {
    @Insert
    suspend fun insert(routine: Routine): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(routineExercises: List<RoutineExerciseEntity>)

    @Update
    suspend fun update(routine: Routine)

    @Delete
    suspend fun delete(routineExercises: List<RoutineExerciseEntity>)

    @Delete
    suspend fun delete(routine: Routine)

    @Query("SELECT * FROM routine_table WHERE id = :key")
    suspend fun getRoutine(key: Long): Routine?

    @Query("SELECT * FROM exercise_table ORDER BY name COLLATE NOCASE ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM routine_exercise_table WHERE routineId = :routineId ORDER BY `order`")
    fun getRoutineExercises(routineId: Long): List<RoutineExerciseEntity>

    @Query(
        """
        SELECT exerciseId, name, duration
        FROM routine_exercise_table JOIN exercise_table ON exerciseId = exercise_table.id 
        WHERE routineId = :routineId ORDER BY `order`
        """
    )
    suspend fun getRoutineExerciseNames(routineId: Long): List<RoutineExercise>

    @Transaction
    suspend fun createRoutine(routineName: String, exercises: List<RoutineExercise>) {
        val newRoutineId = insert(Routine(routineName))
        var order = 1
        insertRoutineExercises(exercises.map {
            RoutineExerciseEntity(newRoutineId, order++, it.exerciseId, it.duration)
        })
    }

    @Transaction
    suspend fun updateRoutine(
        routine: Routine,
        updatedExercises: List<RoutineExerciseEntity>,
        deletedExercises: List<RoutineExerciseEntity>
    ) {
        update(routine)
        insertRoutineExercises(updatedExercises)
        delete(deletedExercises)
    }
}

@Dao
interface SessionDao {
    @Insert
    suspend fun insertHistoryItems(exerciseHistories: List<ExerciseHistory>)

    @Insert
    suspend fun createSession(exercises: List<SessionExercise>)

    @Insert
    suspend fun insert(sessionHistory: SessionHistoryEntity): Long

    @Update
    suspend fun update(sessionExercise: SessionExercise)

    @Query("DELETE FROM saved_session_table")
    fun clearSavedSession()

    @Query("SELECT name FROM routine_table WHERE id = :key")
    suspend fun getRoutineName(key: Long): String

    @Query("SELECT * FROM saved_session_table")
    suspend fun getSavedSession(): MutableList<SessionExercise>

    @Query("SELECT EXISTS(SELECT * FROM exercise_table WHERE id = :exerciseId)")
    fun exerciseExists(exerciseId: Long): Boolean

    @Query(
        """
        SELECT `order`, routine_exercise_table.exerciseId, name, duration, MAX(bpm) AS bpmRecord, "" AS newBpm
        FROM routine_exercise_table 
        JOIN exercise_table ON routine_exercise_table.exerciseId = exercise_table.id 
        LEFT OUTER JOIN exercise_history_table ON routine_exercise_table.exerciseId = exercise_history_table.exerciseId 
        WHERE routineId = :routineId 
        GROUP BY `order`
        ORDER BY `order`
        """
    )
    suspend fun getSessionExercises(routineId: Long): MutableList<SessionExercise>

    @Transaction
    suspend fun completeSession(
        exerciseHistories: List<SessionExercise>,
        sessionHistory: SessionHistoryEntity,
        time: Long
    ) {
        val id = insert(sessionHistory)
        insertHistoryItems(exerciseHistories.map {
            ExerciseHistory(it.exerciseId, id, it.newBpm.toInt(), time)
        })
    }
}

@Dao
interface ExerciseDetailDao {
    @Update
    suspend fun update(exercise: Exercise)

    @Delete
    suspend fun delete(exercise: Exercise)

    @Query("SELECT * FROM exercise_table WHERE id = :key")
    fun getExercise(key: Long): Flow<Exercise?>

    @Query(
        """
        SELECT time, MAX(bpm) as bpm 
        FROM exercise_history_table
        WHERE exerciseId = :exerciseId AND time > :startTime
        GROUP BY time
        ORDER by time
        """
    )
    suspend fun getExerciseHistorySince(
        exerciseId: Long,
        startTime: Long
    ): List<HistoryGraphDataPoint>
}

@Dao
interface HistoryDao {
    @Delete
    suspend fun delete(history: SessionHistoryEntity)

    @Query("SELECT * FROM session_history_table WHERE id = :id")
    suspend fun getSessionHistory(id: Long): SessionHistoryEntity?

    @Query("SELECT * FROM session_history_table ORDER BY time DESC")
    fun getSessionHistories(): PagingSource<Int, SessionHistoryEntity>
}

@Dao
interface RoutineListDao {
    @Query("SELECT * FROM routine_table ORDER BY name COLLATE NOCASE ASC")
    fun getAllRoutines(): Flow<List<Routine>>

    @Query(
        """
        SELECT name
        FROM routine_exercise_table JOIN exercise_table ON exerciseId = exercise_table.id 
        WHERE routineId = :routineId ORDER BY `order`
        """
    )
    suspend fun getRoutinePreview(routineId: Long): List<String>

    fun getRoutinesWithPreviews(): Flow<List<RoutineDisplay>> {
        return getAllRoutines().map { list ->
            list.map { routine -> RoutineDisplay(routine, getRoutinePreview(routine.id)) }
        }
    }
}

@Dao
interface SummaryDao {

    @Query(
        """
            SELECT MAX(time) as time, title, exercises, bpms, improvements, id 
            FROM session_history_table 
            """
    )
    fun getLatestHistory(): Flow<SessionHistoryEntity>
}