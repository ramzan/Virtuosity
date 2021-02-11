package com.nazmar.musicgym.routine.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.nazmar.musicgym.data.RoutineListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RoutineListViewModel @Inject constructor(val useCase: RoutineListUseCase) : ViewModel() {

    var routines = useCase.getAllRoutines().asLiveData()
}
