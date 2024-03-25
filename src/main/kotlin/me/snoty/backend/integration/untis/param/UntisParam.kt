package me.snoty.backend.integration.untis.param

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement


@Serializable(with = UntisParamSerializer::class)
open class UntisParam

object UntisParamSerializer : JsonContentPolymorphicSerializer<UntisParam>(UntisParam::class) {
	override fun selectDeserializer(element: JsonElement) =
		throw UnsupportedOperationException("This class is not supposed to be deserialized directly")
}

@Serializable
data class UntisAuth(
	val user: String,
	val otp: Long,
	val clientTime: Long
)

@Serializable
data class UserDataParams(
	val elementId: Int = 0,
	val deviceOs: String = "AND",
	val deviceOsVersion: String = "",
	val auth: UntisAuth
) : UntisParam()
