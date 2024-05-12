package me.snoty.integration.common.diff

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.atomic.AtomicLong


private const val NAMESPACE = "entity.diff"

class EntityDiffMetrics(
	private val registry: MeterRegistry,
	private val database: Database,
	private val service: String,
	private val entityStateTable: EntityStateTable<*>
) {
	private val byStatus = DiffResult::class.sealedSubclasses.associateWith {
		val name = it.simpleName!!.lowercase()
		Counter.builder("$NAMESPACE.status.$name")
			.description("Number of $name entities")
			.tag("service", service)
			.register(registry)
	}

	fun process(diffResult: DiffResult) {
		byStatus[diffResult::class]?.increment()
	}

	val storedEntities = registry
		.gauge("$NAMESPACE.total", Tags.of("service", service), AtomicLong(-1))!!

	inner class Job : Runnable {
		override fun run() {
			val entityCount = transaction(database) {
				entityStateTable.selectAll()
					.count()
			}
			storedEntities.set(entityCount)
		}
	}
}
