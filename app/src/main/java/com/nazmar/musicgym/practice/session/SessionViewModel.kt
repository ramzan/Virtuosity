package com.nazmar.musicgym.practice.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nazmar.musicgym.db.ExerciseDatabase

class SessionViewModel(routineId: Long, application: Application) : AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    val exercises = dao.getSessionExercises(routineId)
}