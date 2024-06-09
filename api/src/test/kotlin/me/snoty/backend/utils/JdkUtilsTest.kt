package me.snoty.backend.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JdkUtilsTest {
	@Test
	fun testResolveClassName() {
		assertEquals("java.lang.Integer", resolveClassName(Int::class))
		assertEquals("java.lang.String", resolveClassName(String::class))
		assertEquals("java.lang.Long", resolveClassName(Long::class))
	}
}
