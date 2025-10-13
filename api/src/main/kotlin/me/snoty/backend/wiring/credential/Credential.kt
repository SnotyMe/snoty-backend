package me.snoty.backend.wiring.credential

abstract class Credential {
	override fun toString() = "<credential>"
}

data class ResolvedCredential<T : Credential>(
	val id: String,
	val type: String,
	val data: T,
)
