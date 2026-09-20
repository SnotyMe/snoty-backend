package me.snoty.backend.schema

import kotlinx.serialization.Serializable

@Serializable
data class SchemaField(
	val name: String,
	val type: String,
	val defaultValue: String?,
	val displayName: String,
	val description: String?,
	val hidden: Boolean,
	val censored: Boolean,
	val details: SchemaFieldDetails?
)
