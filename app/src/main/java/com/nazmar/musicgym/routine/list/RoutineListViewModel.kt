package com.nazmar.musicgym.routine.list

import androidx.lifecycle.ViewModel
import com.nazmar.musicgym.data.Repository

class RoutineListViewModel : ViewModel() {

    val sessionSaved = Repository.sessionSaved

    var routines = Repository.getAllRoutines()
}
