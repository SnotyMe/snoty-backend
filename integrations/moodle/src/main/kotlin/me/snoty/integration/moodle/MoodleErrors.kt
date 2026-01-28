package me.snoty.integration.moodle

import kotlinx.serialization.Serializable

@Serializable
data class MoodleRpcException(
	val errorcode: String,
	val message: String,
)

fun MoodleRpcException.map() = when (errorcode) {
	"invalidtoken" -> MoodleInvalidTokenException()
	else -> MoodleUnknownException(errorcode, message)
}

abstract class MoodleException(message: String) : Exception("Moodle API error: $message")
class MoodleInvalidTokenException : MoodleException("invalid token provided")
class MoodleUnknownException(
	errorCode: String,
	message: String,
) : MoodleException("$errorCode - $message")
