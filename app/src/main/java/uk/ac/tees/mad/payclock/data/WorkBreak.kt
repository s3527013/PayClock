package uk.ac.tees.mad.payclock.data

data class WorkBreak(
    var id: String,
    var startTime: String,
    var endTime: String?,
    var duration: String?,
    var isPaid: Boolean,
    var isCanceled: Boolean,
)
