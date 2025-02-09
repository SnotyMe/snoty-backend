package me.snoty.backend.database.mongo.migrations.impl

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import me.snoty.backend.database.mongo.migrations.MongoMigration
import me.snoty.integration.common.wiring.flow.FLOW_EXECUTION_COLLECTION_NAME
import me.snoty.integration.common.wiring.flow.FlowExecution
import org.koin.core.annotation.Single

@Single
class MigrateFlowTriggerReason : MongoMigration("0.3.1") {
	override val name = "MigrateFlowTriggerReason"

	private val reasons = listOf("Scheduled", "Manual", "Unknown")
	
	private fun getCollection(database: MongoDatabase) =
		database.getCollection<FlowExecution>(FLOW_EXECUTION_COLLECTION_NAME)
	
	override suspend fun execute(database: MongoDatabase) {
		val collection = getCollection(database)
		reasons.forEach { 
			collection.updateMany(
				Filters.eq("triggeredBy._t", it),
				Updates.set("triggeredBy", it)
			)
		}
	}

	override suspend fun rollback(database: MongoDatabase) {
		val collection = getCollection(database)
		reasons.forEach {
			collection.updateMany(
				Filters.eq("triggeredBy", it),
				Updates.rename("triggeredBy", "ogTriggeredBy")
			)
			collection.updateMany(
				Filters.eq("ogTriggeredBy", it),
				Updates.set("triggeredBy", it)
			)
			collection.updateMany(
				Filters.eq("ogTriggeredBy", it),
				Updates.unset("ogTriggeredBy")
			)
		}
	}
}