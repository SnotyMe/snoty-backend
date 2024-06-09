package me.snoty.integration.common.diff.state

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.AggregateFlow
import me.snoty.backend.database.mongo.Stages
import me.snoty.backend.database.mongo.aggregate
import org.bson.BsonNull

data class UserEntityStatesStats(val totalEntities: Long)

fun EntityStateCollection.getStatistics(): AggregateFlow<UserEntityStatesStats> {
	val entitiesValues = "entitiesValues"
	val entityArray = "entityArray"

	return aggregate<UserEntityStatesStats>(
		Aggregates.project(
			Projections.fields(
				Projections.excludeId(),
				Projections.computed(entitiesValues, Stages.objectToArray("\$${UserEntityStates::entities.name}"))
			)
		),
		Aggregates.unwind("\$$entitiesValues"),
		Aggregates.project(
			Projections.fields(
				Projections.computed(entityArray, "\$$entitiesValues.v")
			)
		),
		Aggregates.unwind("\$${entityArray}"),
		Aggregates.group(
			// group everything into one result
			BsonNull(),
			Accumulators.sum(UserEntityStatesStats::totalEntities.name, 1)
		)
	)
}
