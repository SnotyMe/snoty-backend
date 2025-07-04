package me.snoty.integration.untis.model

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.snoty.backend.utils.DateTimeParseException
import me.snoty.integration.untis.model.UntisDateTimeTest.DateWrapper.Companion.decode
import me.snoty.integration.untis.model.UntisDateTimeTest.DateWrapper.Companion.makeJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Instant

class UntisDateTimeTest {
	@Serializable
	data class DateWrapper(val dateTime: @Serializable(with = ViennaSerializer::class) UntisDateTime) {
		object ViennaSerializer : UntisDateTime.Serializer(timeZone = TimeZone.of("Europe/Vienna"))
		companion object {
			fun makeJson(date: String) = """{"dateTime":"$date"}"""
			fun decode(date: String): UntisDateTime {
				return Json
					.decodeFromString<DateWrapper>(makeJson(date)).dateTime
			}
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
		fun test(expectedUtcDate: String, untisDate: String) {
			assertEquals(expectedUtcDate, decode(untisDate).dateTime.toString())
		}

		// Untis returns it suffixed with a Z, but removing it should work as well
		listOf("", "Z").forEach { suffix ->
			// CEST = UTC + 2 => UTC (snoty) = CEST (untis) - 2
			test(
				"2024-07-27T07:45:00Z",
				"2024-07-27T09:45"
			)
			// CET = UTC + 1 => UTC (snoty) = CEST (untis) - 1
			test(
				"2024-11-06T08:45:00Z",
				"2024-11-06T09:45"
			)
		}
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
