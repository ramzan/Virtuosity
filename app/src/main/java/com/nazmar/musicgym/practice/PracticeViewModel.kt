package com.nazmar.musicgym.practice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nazmar.musicgym.db.ExerciseDatabase

class PracticeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    var routines = dao.getAllRoutines()
}
