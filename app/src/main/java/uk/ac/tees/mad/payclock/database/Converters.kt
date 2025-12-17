package uk.ac.tees.mad.payclock.database

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.util.Date

/**
 * A class that provides type converters for Room.
 */
class Converters {

    // Existing converters for Date
    /**
     * Converts a timestamp to a Date.
     *
     * @param value The timestamp to convert.
     * @return The converted Date, or null if the timestamp is null.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Converts a Date to a timestamp.
     *
     * @param date The Date to convert.
     * @return The converted timestamp, or null if the Date is null.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // New converters for Firebase Timestamp
    /**
     * Converts a map to a Firebase Timestamp.
     *
     * @param value The map to convert.
     * @return The converted Firebase Timestamp, or null if the map is null.
     */
    @TypeConverter
    fun fromFirebaseTimestamp(value: Map<String, Any>?): Timestamp? {
        return value?.let {
            val seconds = it["seconds"] as? Long ?: 0L
            val nanoseconds = it["nanoseconds"] as? Int ?: 0
            Timestamp(seconds, nanoseconds)
        }
    }

    /**
     * Converts a Firebase Timestamp to a map.
     *
     * @param timestamp The Firebase Timestamp to convert.
     * @return The converted map, or null if the Firebase Timestamp is null.
     */
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
    /**
     * Converts a Date to a Firebase Timestamp.
     *
     * @param date The Date to convert.
     * @return The converted Firebase Timestamp, or null if the Date is null.
     */
    @TypeConverter
    fun dateToFirebaseTimestamp(date: Date?): Timestamp? {
        return date?.let { Timestamp(it) }
    }

    /**
     * Converts a Firebase Timestamp to a Date.
     *
     * @param timestamp The Firebase Timestamp to convert.
     * @return The converted Date, or null if the Firebase Timestamp is null.
     */
    @TypeConverter
    fun firebaseTimestampToDate(timestamp: Timestamp?): Date? {
        return timestamp?.toDate()
    }
}