package me.snoty.backend.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringUtilsTest {
	@Test
	fun `test toTitleCase lowerPascalCase`() {
		assertEquals("Title Case", "titleCase".toTitleCase())
		assertEquals("Titlecase", "titlecase".toTitleCase())
		assertEquals("Title Case", "TitleCase".toTitleCase())
		assertEquals("My Very Long Field", "myVeryLongField".toTitleCase())
		assertEquals("My Field With 1 Number", "myFieldWith1Number".toTitleCase())
		assertEquals("My 1 Number Field Lol", "my1NumberFieldLOL".toTitleCase())
	}

	@Test
	fun `test toTitleCase snake_case SCREAMING_SNAKE_CASE`() {
		assertEquals("Snake Case", "snake_case".toTitleCase())
		assertEquals("Screaming Snake Case", "SCREAMING_SNAKE_CASE".toTitleCase())
		assertEquals("Snake Case With 1 Number", "snake_case_with_1_number".toTitleCase())
		assertEquals("Screaming Snake Case With 1 Number", "SCREAMING_SNAKE_CASE_WITH_1_NUMBER".toTitleCase())
	}
}
