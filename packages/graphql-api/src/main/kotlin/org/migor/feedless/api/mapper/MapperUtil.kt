package org.migor.feedless.api.mapper

import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Utility class for MapStruct mappers to use in Java expressions.
 * These are static methods that MapStruct-generated Java code can call.
 */
object MapperUtil {
    @JvmStatic
    fun toMillis(dateTime: LocalDateTime?): Long? {
        return dateTime?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    }

    @JvmStatic
    fun arrayToList(array: Array<String>?): List<String> {
        return array?.toList() ?: emptyList()
    }
}
