package me.snoty.integration.untis.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UntisUserData(
	val displayName: String,
	@SerialName("elemId") val id: Int,
	@SerialName("elemType") val type: String
)
