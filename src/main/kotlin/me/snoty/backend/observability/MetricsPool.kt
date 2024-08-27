package me.snoty.backend.observability

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

const val METRICS_POOL = "metricsPool"

@Single
@Named(METRICS_POOL)
fun provideMetricsPool(): ScheduledExecutorService {
	return Executors.newScheduledThreadPool(1)
}
