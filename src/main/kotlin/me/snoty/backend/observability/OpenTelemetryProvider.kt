package me.snoty.backend.observability

import io.opentelemetry.api.GlobalOpenTelemetry
import org.koin.core.annotation.Single

@Single
fun provideOpenTelemetry() = GlobalOpenTelemetry.get()
