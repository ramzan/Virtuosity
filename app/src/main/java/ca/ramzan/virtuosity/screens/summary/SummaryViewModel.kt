package ca.ramzan.virtuosity.screens.summary

import androidx.lifecycle.ViewModel
import ca.ramzan.virtuosity.summary.SummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(val useCase: SummaryUseCase) : ViewModel() {

    val summary = useCase.history
}