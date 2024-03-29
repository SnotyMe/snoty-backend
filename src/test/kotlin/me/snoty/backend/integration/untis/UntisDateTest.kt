package me.snoty.backend.integration.untis

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.snoty.backend.integration.untis.UntisDateTest.DateWrapper.Companion.decode
import me.snoty.backend.integration.untis.UntisDateTest.DateWrapper.Companion.makeJson
import me.snoty.backend.integration.untis.model.UntisDate
import me.snoty.backend.test.assertThrows
import me.snoty.backend.utils.DateParseException
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

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
