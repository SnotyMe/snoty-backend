package me.snoty.backend.server.handler

import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.modules.SerializersModule

interface IHttpStatusException {
	val code: HttpStatusCode
	val message: String
}

/**
 * WARNING: this class is NOT serializable, make sure to cast to IHttpStatusException
 */
open class HttpStatusException(
	override val code: HttpStatusCode,
	override val message: String = code.description
) : Exception("$code: $message"), IHttpStatusException

val httpStatusExceptionModule = SerializersModule {
	polymorphicDefaultSerializer(IHttpStatusException::class) {
		HttpStatusExceptionSerializer
	}
	polymorphicDefaultDeserializer(IHttpStatusException::class) {
		HttpStatusExceptionSerializer
	}
}

object HttpStatusExceptionSerializer : KSerializer<IHttpStatusException> {
	override val descriptor = buildClassSerialDescriptor("HttpStatusException") {
		element<Int>("code")
		element<String>("message")
	}

	override fun serialize(encoder: Encoder, value: IHttpStatusException) {
		encoder.encodeStructure(descriptor) {
			encodeIntElement(descriptor, 0, value.code.value)
			encodeStringElement(descriptor, 1, value.message)
		}
	}

	override fun deserialize(decoder: Decoder): IHttpStatusException {
		throw NotImplementedError("Deserialization of HttpStatusException is not supported")
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
