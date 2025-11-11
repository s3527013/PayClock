package uk.ac.tees.mad.payclock.data.models

import kotlinx.serialization.Serializable

/**
 * Represents a single break taken during a work shift.
 * Timestamps are stored as Longs for easy serialization.
 */
@Serializable
data class WorkBreak(
    val startTime: Long,
    val endTime: Long?
)
