package ca.ramzan.virtuosity.common.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ca.ramzan.virtuosity.exercises.Exercise
import ca.ramzan.virtuosity.exercises.ExerciseHistory
import ca.ramzan.virtuosity.history.SessionHistoryEntity
import ca.ramzan.virtuosity.routine.Routine
import ca.ramzan.virtuosity.routine.RoutineExerciseEntity
import ca.ramzan.virtuosity.session.SessionExercise

@Database(
    entities = [
        Exercise::class,
        ExerciseHistory::class,
        Routine::class,
        RoutineExerciseEntity::class,
        SessionExercise::class,
        SessionHistoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ExerciseDatabase : RoomDatabase() {

    abstract val exerciseDetailDao: ExerciseDetailDao
    abstract val exerciseListDao: ExerciseListDao
    abstract val historyDao: HistoryDao
    abstract val sessionDao: SessionDao
    abstract val routineEditorDao: RoutineEditorDao
    abstract val routineListDao: RoutineListDao
    abstract val summaryDao: SummaryDao

    companion object {
        @Volatile
        private var INSTANCE: ExerciseDatabase? = null

        fun getInstance(context: Context): ExerciseDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ExerciseDatabase::class.java,
                        "exercise_database"
                    )
                        .createFromAsset("db/exercise_database")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}