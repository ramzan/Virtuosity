package com.nazmar.musicgym.screens.routinelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nazmar.musicgym.routine.RoutineListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineListViewModel @Inject constructor(val useCase: RoutineListUseCase) : ViewModel() {

    var sessionToStartId: Long? = null

    val state = MutableStateFlow<RoutineListState>(RoutineListState.Loading)

    init {
        viewModelScope.launch {
            useCase.getAllRoutines().collect { list ->
                state.emit(RoutineListState.Loaded(list.map {
                    RoutineListCard.RoutineCard(
                        it.id,
                        it.name
                    )
                }))
            }
        }
    }
}

sealed class RoutineListState {
    object Loading : RoutineListState()

    data class Loaded(val routineCards: List<RoutineListCard>) : RoutineListState()
}