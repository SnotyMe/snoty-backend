package me.snoty.backend.observability

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.data.LinkData
import io.opentelemetry.sdk.trace.samplers.Sampler
import io.opentelemetry.sdk.trace.samplers.SamplingResult
import org.koin.core.Koin

class OpenTelemetrySampler(val og: Sampler, koin: Koin) : Sampler by og {
	// lazy injection to avoid circular dependency between OpenTelemetry and OpenFeature
	val featureFlags: OpenTelemetryFeatureFlags by koin.inject()
	
	override fun shouldSample(
		parentContext: Context,
		traceId: String,
		name: String,
		spanKind: SpanKind,
		attributes: Attributes,
		parentLinks: List<LinkData?>
	): SamplingResult? = when {
		// flagd evaluations are dropped. not doing so will cause a recursive loop due to the feature flag evaluations in this function
		name == "resolve" -> SamplingResult.drop()
		featureFlags.muteJobRunrQueries && attributes[DB_SQL_TABLE]?.startsWith("jobrunr") == true -> SamplingResult.drop()

		else -> og.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks)
	}
}
