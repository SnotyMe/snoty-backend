package me.snoty.backend.observability

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.semconv.ServiceAttributes
import me.snoty.backend.build.BuildInfo
import org.koin.core.Koin
import org.koin.core.annotation.Single


@Single(binds = [OpenTelemetry::class])
fun provideOpenTelemetry(koin: Koin, config: OtelConfig, buildInfo: BuildInfo): OpenTelemetry {
	val logger = KotlinLogging.logger {}

	val metadata = Resource.create(
		Attributes.of(
			ServiceAttributes.SERVICE_NAME, buildInfo.application,
			ServiceAttributes.SERVICE_VERSION, buildInfo.version,
		)
	)

	val openTelemetry =
		getManualOpenTelemetry(koin, config, metadata)
			?: run {
				logger.debug { "Manual OpenTelemetry is disabled, using global instance..." }
				getGlobalOpenTelemetry()
			} ?: run {
				logger.debug { "No global OpenTelemetry instance found, auto configuring..." }
				getAutoConfiguredOpenTelemetry(koin, metadata)
			} ?: run {
				logger.debug { "No OpenTelemetry instance found, using noop instance..." }
				OpenTelemetry.noop()
			}

	OpenTelemetryAppender.install(openTelemetry)

	logger.debug { "OpenTelemetry initialized!" }

	return openTelemetry
}

private fun OpenTelemetry.orNull() = takeIf { it != OpenTelemetry.noop() }

private fun getGlobalOpenTelemetry() = GlobalOpenTelemetry.get().orNull()

private fun getAutoConfiguredOpenTelemetry(koin: Koin, metadata: Resource) = AutoConfiguredOpenTelemetrySdk.builder()
	.addResourceCustomizer { resource, _ ->
		resource.merge(metadata).apply {
			KotlinLogging.logger {}.debug { "Created resource for OpenTelemetry: $this" }
		}
	}
	.addSamplerCustomizer { og, _ ->
		OpenTelemetrySampler(og, koin)
	}
	.build()
	// will default to NOOP -> non-null
	.openTelemetrySdk
	.orNull()
