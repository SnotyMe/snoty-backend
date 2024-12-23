package me.snoty.integration.builtin.http

import kotlinx.serialization.Serializable
import me.snoty.integration.common.model.metadata.FieldDefaultValue
import me.snoty.integration.common.wiring.node.NodeSettings
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
)

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
	val statusCode: Int,
	val headers: Map<String, String>,
	/**
	 * The body of the response.
	 * Either a raw string or a parsed JSON object.
	 */
	val body: Any,
)
