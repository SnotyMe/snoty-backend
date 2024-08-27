package me.snoty.backend.observability

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.koin.core.annotation.Single

@Single
fun provideMeterRegistry(): MeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
