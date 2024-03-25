package me.snoty.backend.integration.untis.auth

import me.snoty.backend.integration.untis.helpers.Base32.decode
import me.snoty.backend.integration.untis.param.UntisAuth
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object UntisAuthentication {
	@Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
	private fun verifyCode(key: ByteArray, time: Long): Int {
		var t = time

		val arrayOfByte = ByteArray(8)
		var i = 8

		while (--i > 0) {
			arrayOfByte[i] = t.toByte()
			t = t ushr 8
		}

		val localMac = Mac.getInstance("HmacSHA1")
		localMac.init(SecretKeySpec(key, "HmacSHA1"))
		val hashedKey = localMac.doFinal(arrayOfByte)
		val k = hashedKey[19].toInt()
		t = 0L
		i = 0
		while (i < 4) {
			val l = hashedKey[(k and 0xF) + i].toInt() and 0xFF
			i += 1
			t = t shl 8 or l.toLong()
		}
		return ((t and 0x7FFFFFFF) % 1000000L).toInt()
	}

	fun createAuthObject(user: String, key: String? = null): UntisAuth {
		val timestamp = System.currentTimeMillis()
		return UntisAuth(user, createTimeBasedCode(timestamp, key), timestamp)
	}

	private fun createTimeBasedCode(timestamp: Long, secret: String?): Long {
		return try {
			if (secret?.isNotEmpty() == true)
				verifyCode(
					decode(secret.uppercase(Locale.ROOT)),
					timestamp / 30000L
				).toLong() // Code will change every 30000 milliseconds
			else
				0L
		} catch (e: Exception) {
			0L
		}
	}
}
