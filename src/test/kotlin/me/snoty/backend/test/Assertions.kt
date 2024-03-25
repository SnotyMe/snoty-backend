package me.snoty.backend.test

import org.junit.Assert
import org.junit.function.ThrowingRunnable

inline fun <reified T : Throwable> assertThrows(block: ThrowingRunnable) {
	Assert.assertThrows(T::class.java, block)
}
