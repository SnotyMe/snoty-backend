package me.snoty.integration.moodle

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.reflect.*
import me.snoty.integration.common.SnotyJson
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

class MoodleAPIImpl(client: HttpClient? = null) : MoodleAPI {
	private val logger = KotlinLogging.logger {}
	private val httpClient = client ?: HttpClient(Apache) {
		install(ContentNegotiation) {
			json(SnotyJson)
		}
	}

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

		return response.body(type)
	}
}

class MoodleRequest(private val urlBuilder: URIBuilder, private val settings: MoodleSettings) {
	lateinit var method: String
	val params: MutableList<MoodleParam> = mutableListOf()

	constructor(settings: MoodleSettings, block: MoodleRequest.() -> Unit = {}) : this(
		URIBuilder(settings.baseUrl + MOODLE_WS),
		settings
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
		.setParameter("wstoken", settings.appSecret)
		.setParameter("wsfunction", method)
		.setParameters(params)
		.build()
}
