package me.snoty.integration.untis

import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.snoty.backend.utils.DateTimeParseException
import me.snoty.integration.untis.UntisDateTimeTest.DateWrapper.Companion.decode
import me.snoty.integration.untis.UntisDateTimeTest.DateWrapper.Companion.makeJson
import me.snoty.integration.untis.model.UntisDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UntisDateTimeTest {
	@Serializable
	data class DateWrapper(val dateTime: UntisDateTime) {
		companion object {
			fun makeJson(date: String) = """{"dateTime":"$date"}"""
			fun decode(date: String) = Json.decodeFromString<DateWrapper>(makeJson(date)).dateTime
		}
	}

	@Test
	fun testSerialization() {
		var dateStr = "2024-01-01T10:55:00Z"
		var date = Instant.parse(dateStr)
		var untisDate = UntisDateTime(date)
		assertEquals(untisDate.toLocalDateTime(), date.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime())
		assertEquals(makeJson(dateStr), Json.encodeToString(DateWrapper(untisDate)))

		dateStr = "2023-12-31T23:59:00Z"
		date = Instant.parse(dateStr)
		untisDate = UntisDateTime(date)
		assertEquals(untisDate.toLocalDateTime(), date.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime())
		assertEquals(makeJson(dateStr), Json.encodeToString(DateWrapper(untisDate)))
	}

	@Test
	fun testDeserialization_valid() {
		var date = "2024-04-01T10:55Z"
		assertEquals(UntisDateTime(Instant.parse(date)), decode(date))
		date = "2023-12-31T23:59Z"
		assertEquals(UntisDateTime(Instant.parse(date)), decode(date))
	}

	@Test
	fun testDeserialization_invalid_hasNoTime() {
		assertThrows<DateTimeParseException> {
			decode(makeJson("2024-04-01"))
		}
	}

	@Test
	fun testDeserialization_invalid_notADate() {
		assertThrows<DateTimeParseException> {
			decode(makeJson("invalid"))
		}
	}
}
