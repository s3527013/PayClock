package uk.ac.tees.mad.payclock.data.repository

import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.payclock.data.db.TimeLogDao
import uk.ac.tees.mad.payclock.data.models.TimeLog
import uk.ac.tees.mad.payclock.data.models.TimeLogWithJob

/**
 * Repository for managing TimeLog data using the Room database.
 */
class TimeLogRepository(private val timeLogDao: TimeLogDao) {

    /**
     * Retrieves all time logs with their associated job names.
     */
    val allTimeLogsWithJob: Flow<List<TimeLogWithJob>> = timeLogDao.getAllTimeLogsWithJobName()

    /**
     * Retrieves the currently active time log with its job name, if any.
     */
    val activeTimeLogWithJob: Flow<TimeLogWithJob?> = timeLogDao.getActiveTimeLogWithJobName()

    suspend fun insert(timeLog: TimeLog) {
        timeLogDao.insertTimeLog(timeLog)
    }

    suspend fun update(timeLog: TimeLog) {
        timeLogDao.updateTimeLog(timeLog)
    }

    suspend fun delete(timeLog: TimeLog) {
        timeLogDao.deleteTimeLog(timeLog)
    }
}
