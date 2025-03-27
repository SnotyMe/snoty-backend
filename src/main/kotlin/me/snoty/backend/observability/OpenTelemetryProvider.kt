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
import me.snoty.backend.config.Config
import me.snoty.backend.config.OpenTelemetryConfig
import org.koin.core.annotation.Single
import java.io.FileInputStream
import java.util.*


@Single(binds = [OpenTelemetry::class])
fun provideOpenTelemetry(config: Config, buildInfo: BuildInfo, featureFlags: OpenTelemetryFeatureFlags): OpenTelemetry {
	val logger = KotlinLogging.logger {}
	val openTelemetry = (getGlobalOpenTelemetry()
		?: run {
			logger.debug { "No global OpenTelemetry instance found, auto configuring..." }
			getAutoConfiguredOpenTelemetry(config.openTelemetry, buildInfo, featureFlags)
		})

	OpenTelemetryAppender.install(openTelemetry)

	logger.debug { "OpenTelemetry initialized!" }

	return openTelemetry
}

private fun getGlobalOpenTelemetry() =
	GlobalOpenTelemetry.get().takeIf { it != OpenTelemetry.noop() }

private fun getAutoConfiguredOpenTelemetry(config: OpenTelemetryConfig, buildInfo: BuildInfo, featureFlags: OpenTelemetryFeatureFlags) = AutoConfiguredOpenTelemetrySdk.builder()
	.addResourceCustomizer { resource, _ ->
		var metadata = Resource.create(
			Attributes.of(
				ServiceAttributes.SERVICE_NAME, buildInfo.application,
				ServiceAttributes.SERVICE_VERSION, buildInfo.version,
			)
		)

		config.resourcePaths.forEach { path ->
			runCatching {
				val props = Properties().apply { load(FileInputStream(path)) }
				val attributes = props.entries.fold(Attributes.builder()) { builder, entry ->
					builder.put(entry.key.toString(), entry.value.toString())
				}.build()
				metadata = metadata.merge(Resource.create(attributes))
			}
		}
		resource.merge(metadata).apply {
			KotlinLogging.logger {}.debug { "Created resource for OpenTelemetry: $this" }
		}
	}
	.addSamplerCustomizer { og, _ ->
		OpenTelemetrySampler(og, featureFlags)
	}
	.build()
	// will default to NOOP -> non-null
	.openTelemetrySdk!!
