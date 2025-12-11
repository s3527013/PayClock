package uk.ac.tees.mad.payclock.database

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.util.Date

class Converters {

    // Existing converters for Date
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // New converters for Firebase Timestamp
    @TypeConverter
    fun fromFirebaseTimestamp(value: Map<String, Any>?): Timestamp? {
        return value?.let {
            val seconds = it["seconds"] as? Long ?: 0L
            val nanoseconds = it["nanoseconds"] as? Int ?: 0
            Timestamp(seconds, nanoseconds)
        }
    }

    @TypeConverter
    fun timestampToMap(timestamp: Timestamp?): Map<String, Any>? {
        return timestamp?.let {
            mapOf(
                "seconds" to it.seconds,
                "nanoseconds" to it.nanoseconds
            )
        }
    }

    // Convert between Date and Firebase Timestamp
    @TypeConverter
    fun dateToFirebaseTimestamp(date: Date?): Timestamp? {
        return date?.let { Timestamp(it) }
    }

    @TypeConverter
    fun firebaseTimestampToDate(timestamp: Timestamp?): Date? {
        return timestamp?.toDate()
    }
}