package me.snoty.integration.common.diff

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import kotlinx.coroutines.runBlocking
import me.snoty.backend.observability.METRIC_PREFIX
import me.snoty.integration.common.diff.state.EntityStateCollection
import me.snoty.integration.common.diff.state.getStatistics
import java.util.concurrent.atomic.AtomicLong


private const val NAMESPACE = "${METRIC_PREFIX}_entity.diff"

class EntityDiffMetrics(
	private val registry: MeterRegistry,
	private val service: String,
	private val entityStateCollection: EntityStateCollection
) {
	private val byStatus = DiffResult::class.sealedSubclasses.associateWith {
		val name = it.simpleName!!.lowercase()
		Counter.builder("$NAMESPACE.status.$name")
			.description("Number of $name entities")
			.tag("service", service)
			.register(registry)
	}

	fun process(diffResults: Collection<DiffResult>) {
		diffResults.forEach(::process)
	}

	fun process(diffResult: DiffResult) {
		byStatus[diffResult::class]?.increment()
	}

	val storedEntities = registry
		.gauge("$NAMESPACE.total", Tags.of("service", service), AtomicLong(-1))!!

	inner class Job : Runnable {
		override fun run() {
			val stats = entityStateCollection.getStatistics()
			runBlocking {
				stats.collect { value ->
					storedEntities.set(value.totalEntities)
				}
			}
		}
	}
}
