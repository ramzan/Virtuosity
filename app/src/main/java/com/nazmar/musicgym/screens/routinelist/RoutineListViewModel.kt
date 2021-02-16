package com.nazmar.musicgym.screens.routinelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.nazmar.musicgym.routine.RoutineListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RoutineListViewModel @Inject constructor(val useCase: RoutineListUseCase) : ViewModel() {

    var sessionToStartId: Long? = null

    val routines = useCase.getAllRoutines().asLiveData()
}
