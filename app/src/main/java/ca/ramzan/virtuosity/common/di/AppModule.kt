package ca.ramzan.virtuosity.common.di

import android.app.Application
import android.app.NotificationManager
import android.content.SharedPreferences
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import ca.ramzan.virtuosity.common.room.ExerciseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun db(app: Application) = ExerciseDatabase.getInstance(app)

    @Provides
    @Singleton
    fun exerciseDetailDao(db: ExerciseDatabase) = db.exerciseDetailDao

    @Provides
    @Singleton
    fun exerciseListDao(db: ExerciseDatabase) = db.exerciseListDao

    @Provides
    @Singleton
    fun historyDao(db: ExerciseDatabase) = db.historyDao

    @Provides
    @Singleton
    fun sessionDao(db: ExerciseDatabase) = db.sessionDao

    @Provides
    @Singleton
    fun routineEditorDao(db: ExerciseDatabase) = db.routineEditorDao

    @Provides
    @Singleton
    fun routineListDao(db: ExerciseDatabase) = db.routineListDao

    @Provides
    @Singleton
    fun prefs(app: Application): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(app.applicationContext)

    @Provides
    @Singleton
    fun imm(app: Application): InputMethodManager =
        app.getSystemService(InputMethodManager::class.java)

    @Provides
    @Singleton
    fun notificationManager(app: Application): NotificationManager =
        app.getSystemService(NotificationManager::class.java)
}