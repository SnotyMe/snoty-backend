package me.snoty.backend.server.plugins

import com.sksamuel.cohort.Cohort
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.db.DataSourceManager
import io.ktor.server.application.*

fun Application.configureActuator(
	dataSources: List<DataSourceManager>,
	healthChecks: List<HealthCheck>,
) = install(Cohort) {
	this.dataSources = dataSources
	gc = true
	memory = true

	val healthCheckRegistry = HealthCheckRegistry {
		healthChecks.forEach(::register)
	}

	healthcheck("/health", healthCheckRegistry)
}
