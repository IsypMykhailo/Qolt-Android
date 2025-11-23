package ca.qolt.util

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromBlockedPackages(value: List<String>): String =
        value.joinToString(separator = ",")

    @TypeConverter
    fun toBlockedPackages(value: String): List<String> =
        if (value.isBlank()) emptyList()
        else value.split(",")
}
