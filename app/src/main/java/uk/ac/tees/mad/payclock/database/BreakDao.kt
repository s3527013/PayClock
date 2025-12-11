package uk.ac.tees.mad.payclock.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.payclock.features.breaks.data.Breaks

@Dao
interface BreakDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(breaks: List<Breaks>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(breakEntry: Breaks)

    @Query("SELECT * FROM breaks WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllBreaks(userId: String): Flow<List<Breaks>>

    @Query("SELECT * FROM breaks WHERE userId = :userId AND endTime IS NULL LIMIT 1")
    fun getActiveBreak(userId: String): Flow<Breaks?>
}