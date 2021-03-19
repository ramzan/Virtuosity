package ca.ramzan.virtuosity.screens.routinelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.ramzan.virtuosity.routine.RoutineListUseCase
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
                        it.routine.id,
                        it.routine.name,
                        getPreview(it.exercises)
                    )
                }))
            }
        }
    }

    private fun getPreview(list: List<String>): String {
        var i = 1
        return list.joinToString("\n") { exercise -> "${i++}. $exercise" }
    }
}

sealed class RoutineListState {
    object Loading : RoutineListState()

    data class Loaded(val routineCards: List<RoutineListCard>) : RoutineListState()
}