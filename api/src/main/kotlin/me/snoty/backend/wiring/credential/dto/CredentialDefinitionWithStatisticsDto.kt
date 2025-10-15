package me.snoty.backend.wiring.credential.dto

import kotlinx.serialization.Serializable
import me.snoty.integration.common.model.metadata.ObjectSchema

@Serializable
data class CredentialDefinitionWithStatisticsDto(
	val type: String,
	val displayName: String,
	val schema: ObjectSchema,
	// TODO: add field schema
	val count: Long,
)
