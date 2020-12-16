package com.nazmar.musicgym.practice.routine.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nazmar.musicgym.db.ExerciseDatabase

class RoutineListViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    var routines = dao.getAllRoutines()
}
