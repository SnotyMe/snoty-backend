package me.snoty.backend.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StringUtilsTest {
	@Test
	fun `test toTitleCase`() {
		assertEquals("Title Case", "titleCase".toTitleCase())
		assertEquals("Titlecase", "titlecase".toTitleCase())
		assertEquals("Title Case", "TitleCase".toTitleCase())
		assertEquals("My Very Long Field", "myVeryLongField".toTitleCase())
	}
}
