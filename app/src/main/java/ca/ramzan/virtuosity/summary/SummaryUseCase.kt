package ca.ramzan.virtuosity.summary

import ca.ramzan.virtuosity.common.room.SummaryDao
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SummaryUseCase @Inject constructor(dao: SummaryDao) {

    val history = dao.getLatestHistory().map { it.toSessionHistoryDisplay() }
}