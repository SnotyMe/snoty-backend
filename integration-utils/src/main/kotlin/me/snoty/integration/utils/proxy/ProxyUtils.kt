package me.snoty.integration.utils.proxy

import io.ktor.client.*
import kotlinx.serialization.Serializable
import me.snoty.backend.utils.http.clone
import me.snoty.backend.utils.http.configureProxy
import me.snoty.backend.wiring.credential.Credential
import me.snoty.backend.wiring.credential.RegisterCredential
import me.snoty.integration.common.model.metadata.DisplayName
import me.snoty.integration.common.model.metadata.FieldDefaultValue
import me.snoty.backend.utils.http.ProxyConfig as ApiProxyConfig
import me.snoty.backend.utils.http.ProxyType as ApiProxyType

enum class ProxyType {
	@DisplayName("HTTP")
	HTTP,

	@DisplayName("SOCKS")
	SOCKS,
}

@Serializable
@RegisterCredential("Proxy")
data class ProxyCredential(
	val type: ProxyType,
	val host: String,
	@FieldDefaultValue("8080")
	val port: Int,
) : Credential() {
	fun toProxyConfig() = ApiProxyConfig(
		type = when (type) {
			ProxyType.HTTP -> ApiProxyType.HTTP
			ProxyType.SOCKS -> ApiProxyType.SOCKS
		},
		host = host,
		port = port,
	)
}

fun HttpClient.withOptionalProxy(proxyCredential: ProxyCredential?): HttpClient =
	if (proxyCredential != null) this.withProxy(proxyCredential)
	else this

fun HttpClient.withProxy(proxyCredential: ProxyCredential): HttpClient = this.clone {
	configureProxy(proxyCredential.toProxyConfig())
}
