package me.snoty.backend.errors

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
open class ServiceResult(
	val httpCode: Int,
	val message: String,
	val details: String? = null,
) {
	constructor(
		httpCode: HttpStatusCode,
		message: String,
		details: String? = null,
	) : this(
		httpCode = httpCode.value,
		message = message,
		details = details,
	)
}
