package me.snoty.integration.builtin.http

import io.ktor.client.*
import kotlinx.serialization.Serializable
import me.snoty.integration.common.model.metadata.FieldDefaultValue
import me.snoty.integration.common.model.metadata.FieldDescription
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import io.ktor.http.HttpMethod as KtorHttpMethod

enum class HttpNodeSerializer {
	TEXT,
	JSON,
}

@Serializable
data class HttpNodeSettings(
	override val name: String = "http",

	val serializeOutputAs: HttpNodeSerializer,
	val requests: List<HttpNodeInput>,
) : NodeSettings

@Serializable
data class HttpNodeInput(
	val url: String,
	val method: HttpMethod,
	val headers: Map<@FieldDefaultValue("My-Header") String, @FieldDefaultValue("Header Value") String>,
	val body: String,
	@FieldDescription("Whether to expect the request to succeed. When set to false, non-200 status codes will cause the Flow to fail.")
	@FieldDefaultValue("false")
	val expectSuccess: Boolean = true, // set to true for backwards compatibility
)

fun HttpClient.applyConfig(httpNodeInput: HttpNodeInput) = config {
	expectSuccess = httpNodeInput.expectSuccess
}

enum class HttpMethod {
	GET,
	POST,
	PUT,
	DELETE;

	val ktor
		get() = when (this) {
			GET -> KtorHttpMethod.Get
			POST -> KtorHttpMethod.Post
			PUT -> KtorHttpMethod.Put
			DELETE -> KtorHttpMethod.Delete
		}
}

data class HttpNodeOutput(
	val url: String,
	val statusCode: Int,
	val headers: Map<String, String>,
	/**
	 * The body of the response.
	 * Either a raw string or a parsed JSON object.
	 */
	val body: Any,
)

fun HttpNodeOutput.toDocument() = Document().apply {
	this[HttpNodeOutput::url.name] = url
	this[HttpNodeOutput::statusCode.name] = statusCode
	this[HttpNodeOutput::headers.name] = headers
	// the body may be a Document or a String
	// since bson uses static information to get codecs, it'll try looking up a `java.lang.Object` codec, which obviously doesn't exist
	this[HttpNodeOutput::body.name] = body
}
