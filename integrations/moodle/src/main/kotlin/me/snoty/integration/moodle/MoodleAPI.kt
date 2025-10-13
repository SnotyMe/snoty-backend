package me.snoty.integration.moodle

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.*
import io.ktor.util.reflect.*
import me.snoty.integration.moodle.param.MoodleParam
import org.apache.http.client.utils.URIBuilder
import java.net.URI

interface MoodleAPI {
	suspend fun <T> request(type: TypeInfo, request: MoodleRequest): T
}

/**
 * Bridge to allow for easier usage of the Moodle API interface
 *
 * Interfaces cannot have reified type parameters, so this extension function
 * is a workaround to bridge this gap
 */
suspend inline fun <reified T> MoodleAPI.request(request: MoodleRequest): T = request(typeInfo<T>(), request)

const val MOODLE_WS = "/webservice/rest/server.php"

class MoodleAPIImpl(private val httpClient: HttpClient) : MoodleAPI {
	private val logger = KotlinLogging.logger {}

	override suspend fun <T> request(type: TypeInfo, request: MoodleRequest): T {
		val response = httpClient.post(request.toUri().toASCIIString()) {
			header("Content-Type", "application/json; charset=UTF-8")
		}
		if (logger.isDebugEnabled()) {
			val body = response.bodyAsText()
			logger.debug {
				"Fetched Moodle Data: HTTP ${response.status.value} - $body"
			}
		}

		return try {
			response.body(type)
		} catch (_: JsonConvertException) {
			val rpcException: MoodleRpcException = response.body()
			val exception = rpcException.map()
			throw exception
		}
	}
}

class MoodleRequest(private val urlBuilder: URIBuilder, private val credential: MoodleCredential) {
	lateinit var method: String
	val params: MutableList<MoodleParam> = mutableListOf()

	constructor(credential: MoodleCredential, block: MoodleRequest.() -> Unit = {}) : this(
		urlBuilder = URIBuilder(credential.baseUrl + MOODLE_WS),
		credential = credential,
	) {
		block()
	}

	fun param(param: MoodleParam) {
		params.add(param)
	}

	private fun URIBuilder.setParameters(params: List<MoodleParam>): URIBuilder {
		params.flatMap { it.toMap().entries }
			.forEach { (key, value) ->
				this.setParameter(key, value)
			}
		return this
	}

	fun toUri(): URI = urlBuilder
		.setParameter("moodlewsrestformat", "json")
		.setParameter("wstoken", credential.appSecret)
		.setParameter("wsfunction", method)
		.setParameters(params)
		.build()
}
