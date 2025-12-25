package me.snoty.backend.wiring.credential

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("_t")
abstract class Credential {
	override fun toString() = "<credential>"
}

data class ResolvedCredential<T : Credential>(
	val id: String,
	val type: String,
	val data: T,
)
