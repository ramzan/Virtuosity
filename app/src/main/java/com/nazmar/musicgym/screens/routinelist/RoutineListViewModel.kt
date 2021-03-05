package com.nazmar.musicgym.screens.routinelist

import androidx.lifecycle.ViewModel
import com.nazmar.musicgym.routine.RoutineListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class RoutineListViewModel @Inject constructor(val useCase: RoutineListUseCase) : ViewModel() {

    var sessionToStartId: Long? = null

    val routineCards = useCase.getAllRoutines().map { list ->
        list.map { RoutineListCard.RoutineCard(it.id, it.name) }
    }
}
