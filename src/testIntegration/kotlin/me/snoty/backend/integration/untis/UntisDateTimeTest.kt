package me.snoty.backend.integration.untis

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.snoty.backend.integration.untis.UntisDateTimeTest.DateWrapper.Companion.decode
import me.snoty.backend.integration.untis.UntisDateTimeTest.DateWrapper.Companion.makeJson
import me.snoty.backend.integration.untis.model.UntisDateTime
import me.snoty.backend.test.assertThrows
import me.snoty.backend.utils.DateTimeParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

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
		var dateStr = "2024-01-01T10:55"
		var date = LocalDateTime.parse(dateStr)
		var untisDate = UntisDateTime(date)
		assertEquals(untisDate.toLocalDateTime(), date)
		assertEquals(makeJson(dateStr), Json.encodeToString(DateWrapper(untisDate)))

		dateStr = "2023-12-31T23:59"
		date = LocalDateTime.parse(dateStr)
		untisDate = UntisDateTime(date)
		assertEquals(untisDate.toLocalDateTime(), date)
		assertEquals(makeJson(dateStr), Json.encodeToString(DateWrapper(untisDate)))
	}

	@Test
	fun testDeserialization_valid() {
		var date = "2024-04-01T10:55"
		assertEquals(UntisDateTime(LocalDateTime.parse(date)), decode(date))
		date = "2023-12-31T23:59"
		assertEquals(UntisDateTime(LocalDateTime.parse(date)), decode(date))
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
