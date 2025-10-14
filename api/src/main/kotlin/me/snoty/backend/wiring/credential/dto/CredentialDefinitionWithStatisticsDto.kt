package me.snoty.backend.wiring.credential.dto

import kotlinx.serialization.Serializable

@Serializable
data class CredentialDefinitionWithStatisticsDto(
	val type: String,
	// TODO: add field schema
	val count: Long,
)
