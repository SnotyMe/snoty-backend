package me.snoty.backend.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JdkUtilsTest {
	@Test
	fun test() {
		assertEquals("java.lang.Integer", resolveClassName(Int::class))
	}
}
