package me.snoty.backend.observability

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.koin.core.annotation.Single

@Single
fun provideMeterRegistry(): MeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
