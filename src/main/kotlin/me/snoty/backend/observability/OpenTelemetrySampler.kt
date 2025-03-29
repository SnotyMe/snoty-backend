package me.snoty.backend.observability

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.data.LinkData
import io.opentelemetry.sdk.trace.samplers.Sampler
import io.opentelemetry.sdk.trace.samplers.SamplingResult
import me.snoty.backend.featureflags.FeatureFlagChangeListener
import org.koin.core.Koin

class OpenTelemetrySampler(val og: Sampler, koin: Koin) : Sampler by og {
	// set using listener to avoid recursion
	var muteJobRunrQueries = false
	init {
		val featureFlags: OpenTelemetryFeatureFlags = koin.get()
		FeatureFlagChangeListener(
			client = koin.get(),
			flag = featureFlags.muteJobRunrQueries,
			initialFire = true,
			onChange = { newValue ->
				muteJobRunrQueries = newValue
			}
		)
	}
	
	override fun shouldSample(
		parentContext: Context,
		traceId: String,
		name: String,
		spanKind: SpanKind,
		attributes: Attributes,
		parentLinks: List<LinkData?>
	): SamplingResult? = when {
		muteJobRunrQueries && attributes[DB_SQL_TABLE]?.startsWith("jobrunr") == true -> SamplingResult.drop()

		else -> og.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks)
	}
}
