package me.snoty.backend.utils

import me.snoty.backend.errors.InvalidIdException
import java.security.SecureRandom
import kotlin.time.Clock
import kotlin.uuid.Uuid

private val random = SecureRandom()

/**
 * Generates a new v7 UUID.
 * See https://www.ietf.org/archive/id/draft-peabody-dispatch-new-uuid-format-04.html#name-uuid-version-7
 */
fun Uuid.Companion.randomV7(): Uuid {
	val value = ByteArray(16)
	random.nextBytes(value)

	val timestamp = Clock.System.now().toEpochMilliseconds()

	// timestamp
	value[0] = ((timestamp shr 40) and 0xFF).toByte()
	value[1] = ((timestamp shr 32) and 0xFF).toByte()
	value[2] = ((timestamp shr 24) and 0xFF).toByte()
	value[3] = ((timestamp shr 16) and 0xFF).toByte()
	value[4] = ((timestamp shr 8) and 0xFF).toByte()
	value[5] = (timestamp and 0xFF).toByte()

	// version and variant
	value[6] = (value[6].toInt() and 0x0F or 0x70).toByte()
	value[8] = (value[8].toInt() and 0x3F or 0x80).toByte()

	return Uuid.fromByteArray(value)
}

fun String.toUuid() = try {
	Uuid.parse(this)
} catch (e: IllegalArgumentException) {
	throw InvalidIdException(e)
}
