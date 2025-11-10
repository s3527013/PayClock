package uk.ac.tees.mad.payclock.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.ac.tees.mad.payclock.data.models.WorkBreak
import java.time.Duration
import java.time.Instant

/**
 * Type converters to allow Room to reference complex data types.
 */
class Converters {

    // Converter for Instant <-> Long
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilli()
    }

    // Converter for Duration <-> Long
    @TypeConverter
    fun fromDuration(value: Long?): Duration? {
        return value?.let { Duration.ofMillis(it) }
    }

    @TypeConverter
    fun durationToLong(duration: Duration?): Long? {
        return duration?.toMillis()
    }

    // Converter for List<WorkBreak> <-> String (JSON)
    @TypeConverter
    fun fromWorkBreakList(value: String?): List<WorkBreak>? {
        return value?.let { Json.decodeFromString<List<WorkBreak>>(it) }
    }

    @TypeConverter
    fun toWorkBreakList(list: List<WorkBreak>?): String? {
        return list?.let { Json.encodeToString(it) }
    }
}
