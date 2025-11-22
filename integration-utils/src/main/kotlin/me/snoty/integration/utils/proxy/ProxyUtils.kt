package me.snoty.integration.utils.proxy

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import me.snoty.backend.wiring.credential.Credential
import me.snoty.backend.wiring.credential.RegisterCredential
import me.snoty.integration.common.model.metadata.DisplayName
import me.snoty.integration.common.model.metadata.FieldDefaultValue
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

enum class ProxyType {
	@DisplayName("HTTP")
	HTTP,

	@DisplayName("SOCKS")
	SOCKS,
}

@Serializable
@RegisterCredential("Proxy")
class ProxyCredential(
	val type: ProxyType,
	val host: String,
	@FieldDefaultValue("8080")
	val port: Int,
) : Credential()

@Suppress("UNCHECKED_CAST")
private val userConfigField: KProperty1<HttpClient, HttpClientConfig<OkHttpConfig>> = HttpClient::class.declaredMemberProperties
	.single { it.name == "userConfig" }
	.apply {
		isAccessible = true
	} as KProperty1<HttpClient, HttpClientConfig<OkHttpConfig>>

fun HttpClient.withProxy(proxyCredential: ProxyCredential): HttpClient = HttpClient(OkHttp) {
	val oldConfig = userConfigField.get(this@withProxy)
	this.plusAssign(oldConfig)

	engine {
		val url = URLBuilder().also {
			it.host = proxyCredential.host
			it.port = proxyCredential.port
		}.build()

		proxy = when (proxyCredential.type) {
			ProxyType.HTTP -> ProxyBuilder.http(url)
			ProxyType.SOCKS -> ProxyBuilder.socks(url.host, url.port)
		}
	}
}
