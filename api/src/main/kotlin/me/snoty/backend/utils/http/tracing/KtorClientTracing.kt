package me.snoty.backend.utils.http.tracing

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestData
import io.ktor.client.statement.HttpResponse
import io.ktor.util.AttributeKey
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter
import io.opentelemetry.instrumentation.ktor.client.AbstractKtorClientTracing
import io.opentelemetry.instrumentation.ktor.internal.KtorClientTracingUtil

class KtorClientTracing internal constructor(
	instrumenter: Instrumenter<HttpRequestData, HttpResponse>,
	propagators: ContextPropagators
) : AbstractKtorClientTracing(instrumenter, propagators) {
	companion object : HttpClientPlugin<KtorClientTracingBuilder, KtorClientTracing> {
		override val key = AttributeKey<KtorClientTracing>("OpenTelemetry")

		override fun prepare(block: KtorClientTracingBuilder.() -> Unit)
			= KtorClientTracingBuilder().apply(block).build()

		override fun install(plugin: KtorClientTracing, scope: HttpClient)
			= KtorClientTracingUtil.install(plugin, scope)
	}
}
