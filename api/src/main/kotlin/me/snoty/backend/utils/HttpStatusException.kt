package me.snoty.backend.utils

import io.ktor.http.*
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
@Serializable
open class HttpStatusException(
	val code: @Serializable(with = HttpStatusCodeSerializer::class) HttpStatusCode,
	@EncodeDefault(EncodeDefault.Mode.ALWAYS)
	override val message: String = code.description
) : Exception("$code: $message")

object HttpStatusCodeSerializer : KSerializer<HttpStatusCode> {
	override val descriptor = PrimitiveSerialDescriptor("HttpStatusCode", PrimitiveKind.INT)

	override fun serialize(encoder: Encoder, value: HttpStatusCode) {
		encoder.encodeInt(value.value)
	}

	override fun deserialize(decoder: Decoder): HttpStatusCode {
		val value = decoder.decodeInt()
		return HttpStatusCode.fromValue(value)
	}
}

// 4xx
class BadRequestException(message: String) : HttpStatusException(HttpStatusCode.BadRequest, message)
class UnauthorizedException(message: String) : HttpStatusException(HttpStatusCode.Unauthorized, message)
class ForbiddenException(message: String) : HttpStatusException(HttpStatusCode.Forbidden, message)
class NotFoundException(message: String? = null) : HttpStatusException(HttpStatusCode.NotFound, message ?: HttpStatusCode.NotFound.description)

// 5xx
class InternalServerErrorException(message: String) : HttpStatusException(HttpStatusCode.InternalServerError, message)
class NotImplemented(message: String) : HttpStatusException(HttpStatusCode.NotImplemented, message)
