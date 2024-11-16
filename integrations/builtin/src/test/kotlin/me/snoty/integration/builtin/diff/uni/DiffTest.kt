package me.snoty.integration.builtin.diff.uni

import me.snoty.integration.common.diff.Change
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DiffTest {
	@Test
	fun `basic one liner`() {
		val change = Change("Hello World", "Hallo Welt")
		val diff = computeDiff(change)
		assertEquals("""
			--- old
			+++ new
			@@ -1,1 +1,1 @@
			-Hello World
			+Hallo Welt
		""".trimIndent(), diff)
	}

	@Test
	fun `basic one liner with context`() {
		val change = Change("""
			First
			Second
			Third
			Fourth
			Fifth
		""".trimIndent(), """
			First
			Second
			CHANGED
			Fourth
			Fifth
		""".trimIndent())

		val diff = computeDiff(change)

		assertEquals("""
			--- old
			+++ new
			@@ -1,5 +1,5 @@
			 First
			 Second
			-Third
			+CHANGED
			 Fourth
			 Fifth
		""".trimIndent(), diff)
	}

	@Test
	fun `simple multiple lines`() {
		val change = Change("""
			First
			Second
			Third
			Fourth
			Fifth
			Sixth
		""".trimIndent(), """
			First
			SeCOnd
			CHANGED
			Fourth
			Fifth
			Sixth
		""".trimIndent())

		val diff = computeDiff(change)

		assertEquals("""
			--- old
			+++ new
			@@ -1,5 +1,5 @@
			 First
			-Second
			-Third
			+SeCOnd
			+CHANGED
			 Fourth
			 Fifth
		""".trimIndent(), diff)
	}

	@Test
	fun `complex multiple lines`() {
		val change = Change("""
			First
			Second
			Third
			Fourth
			Fifth
			Sixth
		""".trimIndent(), """
			First
			SeCOnd
			Third
			CHANGED
			Fifth
			Sixth
		""".trimIndent())

		val diff = computeDiff(change)

		assertEquals("""
			--- old
			+++ new
			@@ -1,6 +1,6 @@
			 First
			-Second
			+SeCOnd
			 Third
			-Fourth
			+CHANGED
			 Fifth
			 Sixth
		""".trimIndent(), diff)
	}
}
