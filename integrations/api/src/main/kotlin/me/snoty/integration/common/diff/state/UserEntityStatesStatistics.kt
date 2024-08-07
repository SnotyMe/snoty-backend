package me.snoty.integration.common.diff.state

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.AggregateFlow
import me.snoty.backend.database.mongo.Stages
import me.snoty.backend.database.mongo.aggregate

data class UserEntityStateStats(val totalEntities: Long)

fun EntityStateCollection.getStatistics(): AggregateFlow<UserEntityStateStats> {
	val entitiesValues = "entitiesValues"
	val entityArray = "entityArray"

	return aggregate<UserEntityStateStats>(
		Aggregates.project(
			Projections.fields(
				Projections.excludeId(),
				Projections.computed(entitiesValues, Stages.objectToArray("\$${NodeEntityStates::entities.name}"))
			)
		),
		Aggregates.unwind("\$$entitiesValues"),
		Aggregates.project(
			Projections.computed(entityArray, "\$$entitiesValues.v")
		),
		Aggregates.unwind("\$${entityArray}"),
		Aggregates.count(UserEntityStateStats::totalEntities.name)
	)
}
