package com.nazmar.musicgym.di.app

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.nazmar.musicgym.db.ExerciseDatabase
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
    fun db(application: Application) = ExerciseDatabase.getInstance(application)

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
    fun prefs(application: Application): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(application.applicationContext)

}