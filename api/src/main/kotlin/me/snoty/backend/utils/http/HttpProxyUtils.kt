package me.snoty.backend.utils.http

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.fp.getOrElse
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.http.*
import me.snoty.backend.config.ConfigException
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.ConfigWrapper
import me.snoty.backend.config.loadConfig
import org.koin.core.annotation.Single

enum class ProxyType {
	HTTP,
	SOCKS,
}

data class ProxyConfig(
	val type: ProxyType,
	val host: String,
	val port: Int,
)

@ConfigWrapper
data class ProxyConfigWrapper(val defaultProxy: ProxyConfig?)

@Single
fun provideProxyConfig(configLoader: ConfigLoader): ProxyConfigWrapper = configLoader.loadConfig<ProxyConfig>("defaultProxy")
	.getOrElse { failure ->
		if (failure is ConfigFailure.MissingConfigValue) return@getOrElse null
		throw ConfigException(failure)
	}
	.let(::ProxyConfigWrapper)

fun HttpClientConfig<OkHttpConfig>.configureProxy(proxyConfig: ProxyConfig) {
	engine {
		val url = URLBuilder().also {
			it.host = proxyConfig.host
			it.port = proxyConfig.port
		}.build()

		proxy = when (proxyConfig.type) {
			ProxyType.HTTP -> ProxyBuilder.http(url)
			ProxyType.SOCKS -> ProxyBuilder.socks(url.host, url.port)
		}
	}
}
