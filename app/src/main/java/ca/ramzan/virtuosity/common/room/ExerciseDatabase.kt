package ca.ramzan.virtuosity.common.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import ca.ramzan.virtuosity.common.DEFAULT_TIMER_DURATION
import ca.ramzan.virtuosity.exercises.Exercise
import ca.ramzan.virtuosity.exercises.ExerciseHistory
import ca.ramzan.virtuosity.history.SessionHistoryEntity
import ca.ramzan.virtuosity.routine.Routine
import ca.ramzan.virtuosity.routine.RoutineExerciseEntity
import ca.ramzan.virtuosity.session.SessionExercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                        .fallbackToDestructiveMigration()
                        .addCallback(object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                GlobalScope.launch(Dispatchers.IO) {
                                    withContext(Dispatchers.IO) {
                                        // Prepopulate db
                                        getInstance(context).run {
                                            listOf(
                                                Exercise("A Major Scale", 1),
                                                Exercise("B Major Scale", 2),
                                                Exercise("C Major Scale", 3),
                                                Exercise("D Major Scale", 4),
                                            ).forEach { exerciseListDao.insert(it) }

                                            routineEditorDao.insert(Routine("Sample Routine", 1))

                                            routineEditorDao.insertRoutineExercises(
                                                listOf(
                                                    RoutineExerciseEntity(
                                                        1,
                                                        1,
                                                        1,
                                                        DEFAULT_TIMER_DURATION
                                                    ),
                                                    RoutineExerciseEntity(
                                                        1,
                                                        2,
                                                        2,
                                                        DEFAULT_TIMER_DURATION
                                                    ),
                                                    RoutineExerciseEntity(
                                                        1,
                                                        3,
                                                        3,
                                                        DEFAULT_TIMER_DURATION
                                                    ),
                                                    RoutineExerciseEntity(
                                                        1,
                                                        4,
                                                        4,
                                                        DEFAULT_TIMER_DURATION
                                                    ),
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        })
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}