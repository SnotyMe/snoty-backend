package me.snoty.backend.observability

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.fp.getOrElse
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.samplers.Sampler
import me.snoty.backend.config.ConfigException
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.loadConfig
import org.koin.core.Koin
import org.koin.core.annotation.Single

@Single
fun provideOpenTelemetryConfig(configLoader: ConfigLoader) =
	configLoader.loadConfig<OtelConfig>(prefix = "opentelemetry").getOrElse { failure ->
		if (failure is ConfigFailure.MissingConfigValue) return@getOrElse OtelConfig()
		throw ConfigException(failure)
	}

internal fun getManualOpenTelemetry(koin: Koin, config: OtelConfig, metadata: Resource): OpenTelemetrySdk? {
	if (!config.enabled) return null

	val propagators = config.propagators.map { it.get() }

	// the backing fields of the `setXXXProvider` methods are actually nullable
	@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
	return OpenTelemetrySdk.builder()
		.setPropagators(
			ContextPropagators.create(
				TextMapPropagator.composite(propagators)
			)
		)
		.setTracerProvider(config.traces?.build(koin, metadata))
		.setLoggerProvider(config.logs?.build(metadata))
		.build()
}

val DEFAULT_SAMPLER = Sampler.parentBased(Sampler.alwaysOn())!!

private fun OtelTracingConfig.build(koin: Koin, metadata: Resource): SdkTracerProvider {
	val spanExporter = when (transport) {
		OtelTransport.GRPC -> OtlpGrpcSpanExporter.builder()
			.setEndpoint(endpoint)
			.setHeaders { headers }
			.build()
		OtelTransport.HTTP -> OtlpHttpSpanExporter.builder()
			.setEndpoint(endpoint)
			.setHeaders { headers }
			.build()
	}

	val spanProcessor = when (processor) {
		OtelProcessor.BATCH -> BatchSpanProcessor.builder(spanExporter).build()
		OtelProcessor.SIMPLE -> SimpleSpanProcessor.create(spanExporter)
	}

	return SdkTracerProvider.builder()
		.addSpanProcessor(spanProcessor)
		.addResource(metadata)
		.setSampler(OpenTelemetrySampler(DEFAULT_SAMPLER, koin))
		.build()
}

private fun OtelLoggingConfig.build(metadata: Resource): SdkLoggerProvider {
	val logExporter = when (transport) {
		OtelTransport.GRPC -> OtlpGrpcLogRecordExporter.builder()
			.setEndpoint(endpoint)
			.setHeaders { headers }
			.build()
		OtelTransport.HTTP -> OtlpHttpLogRecordExporter.builder()
			.setEndpoint(endpoint)
			.setHeaders { headers }
			.build()
	}

	val logProcessor = when (processor) {
		OtelProcessor.BATCH -> BatchLogRecordProcessor.builder(logExporter).build()
		OtelProcessor.SIMPLE -> SimpleLogRecordProcessor.create(logExporter)
	}

	return SdkLoggerProvider.builder()
		.addLogRecordProcessor(logProcessor)
		.addResource(metadata)
		.build()
}
