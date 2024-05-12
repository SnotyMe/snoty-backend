package me.snoty.backend.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException as JavaDateTimeParseException

class DateParseException(message: String) : IllegalArgumentException("Invalid date: $message")

object DateUtils {
	fun parseIsoDate(date: String): LocalDate {
		return try {
			LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
		} catch (e: JavaDateTimeParseException) {
			throw DateParseException(e.message ?: "Unknown error")
		}
	}
}

class DateTimeParseException(message: String) : IllegalArgumentException("Invalid date time: $message")

object DateTimeUtils {
	fun parseIsoDateTime(dateTime: String): LocalDateTime {
		return try {
			LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME)
		} catch (e: JavaDateTimeParseException) {
			throw DateTimeParseException(e.message ?: "Unknown error")
		}
	}
}
