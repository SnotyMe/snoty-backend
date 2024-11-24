package me.snoty.backend.wiring.flow.export

import java.security.MessageDigest

fun Any.hash(): String {
	val bytes = (this as? String ?: toString()).toByteArray()
	val md = MessageDigest.getInstance("SHA-256")
	val digest = md.digest(bytes)
	return digest.joinToString("") { "%02x".format(it) }
}
