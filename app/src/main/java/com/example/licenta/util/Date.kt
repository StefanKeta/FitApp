package com.example.licenta.util

import java.sql.Date
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

object Date {
    private const val DATE_PATTERN = "dd MMMM yyyy"
    fun getTimestampFromDate(date: String): Long {
        val parsedDate = LocalDate
            .parse(date, DateTimeFormatter.ofPattern(DATE_PATTERN))
        return parsedDate.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .epochSecond
    }

    fun getDateFromTimestamp(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val formatter = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
        return formatter.format(date)
    }

    fun setCurrentDay(): String {
        val timestamp = LocalDate
            .now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .epochSecond
        return getDateFromTimestamp(timestamp)
    }


    fun goToDayAfter(date: String): String {
        val timestamp = getTimestampFromDate(date)
        val newTimestamp = Instant.ofEpochSecond(timestamp)
            .atZone(ZoneId.systemDefault())
            .plusDays(1)
            .toEpochSecond()
        return getDateFromTimestamp(newTimestamp)
    }


    fun goToDayBefore(date: String): String {
        val timestamp = getTimestampFromDate(date)
        val dayBeforeTimestamp = Instant.ofEpochSecond(timestamp)
            .atZone(ZoneId.systemDefault())
            .minusDays(1)
            .toEpochSecond()
        return getDateFromTimestamp(dayBeforeTimestamp)
    }

    fun parseAge(timestamp: Long): Int {
        val todayTimestamp = LocalDate
            .now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .epochSecond
        return ((todayTimestamp - timestamp) / 60 / 60 / 24 / 365).toInt()
    }

}