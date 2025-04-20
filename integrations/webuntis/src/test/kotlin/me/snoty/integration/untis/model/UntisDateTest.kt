package me.snoty.integration.untis.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.snoty.backend.utils.DateParseException
import me.snoty.integration.untis.model.UntisDateTest.DateWrapper.Companion.decode
import me.snoty.integration.untis.model.UntisDateTest.DateWrapper.Companion.makeJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class UntisDateTest {
	@Serializable
	data class DateWrapper(val date: UntisDate) {
		companion object {
			fun makeJson(date: String) = """{"date":"$date"}"""
			fun decode(date: String) = Json.decodeFromString<DateWrapper>(makeJson(date)).date
		}
	}

	@Test
	fun testSerialization() {
		var dateStr = "2024-04-01"
		var date = LocalDate.parse(dateStr)
		var untisDate = UntisDate(date)
		assertEquals(untisDate.toLocalDate(), date)
		assertEquals(makeJson(dateStr), Json.encodeToString(DateWrapper(untisDate)))

		dateStr = "2023-12-31"
		date = LocalDate.parse(dateStr)
		untisDate = UntisDate(date)
		assertEquals(untisDate.toLocalDate(), date)
		assertEquals(makeJson(dateStr), Json.encodeToString(DateWrapper(untisDate)))
	}

	@Test
	fun testDeserialization_valid() {
		var date = "2024-04-01"
		assertEquals(UntisDate(LocalDate.parse(date)), decode(date))
		date = "2023-12-31"
		assertEquals(UntisDate(LocalDate.parse(date)), decode(date))
	}

	@Test
	fun testDeserialization_invalid_hasTime() {
		assertThrows<DateParseException> {
			decode(makeJson("2024-04-01T00:00:00"))
		}
	}

	@Test
	fun testDeserialization_invalid_notADate() {
		assertThrows<DateParseException> {
			decode(makeJson("invalid"))
		}
	}
}
