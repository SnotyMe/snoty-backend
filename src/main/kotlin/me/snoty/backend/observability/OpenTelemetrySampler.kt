package me.snoty.backend.observability

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.data.LinkData
import io.opentelemetry.sdk.trace.samplers.Sampler
import io.opentelemetry.sdk.trace.samplers.SamplingResult

class OpenTelemetrySampler(val og: Sampler, val featureFlags: OpenTelemetryFeatureFlags) : Sampler by og {
	override fun shouldSample(
		parentContext: Context,
		traceId: String,
		name: String,
		spanKind: SpanKind,
		attributes: Attributes,
		parentLinks: List<LinkData?>
	): SamplingResult? {
		if (featureFlags.muteJobRunrQueries && attributes[DB_SQL_TABLE]?.startsWith("jobrunr") == true) return SamplingResult.drop()

		return og.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks)
	}
}
