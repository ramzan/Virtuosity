package ca.ramzan.virtuosity.summary

import ca.ramzan.virtuosity.common.room.SummaryDao
import javax.inject.Inject

class SummaryUseCase @Inject constructor(dao: SummaryDao) {

    val history = dao.getLatestHistory()
}