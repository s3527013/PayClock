package uk.ac.tees.mad.payclock.features.breaks.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Breaks(
    @DocumentId
    val id: String = "",
    val timeLogId: String = "",
    val userId: String = "",
    @ServerTimestamp
    val startTime: Date? = null,
    var endTime: Date? = null,
    var duration: Long? = null, // Duration in minutes
)