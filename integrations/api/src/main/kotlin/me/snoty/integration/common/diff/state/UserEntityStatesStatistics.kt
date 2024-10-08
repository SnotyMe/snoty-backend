package me.snoty.integration.common.diff.state

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.AggregateFlow
import me.snoty.backend.database.mongo.Aggregations
import me.snoty.backend.database.mongo.aggregate
import me.snoty.backend.database.mongo.mongoField
import org.bson.types.ObjectId

/**
 * @param _id will always be null, just there to make the logger stfu
 */
data class UserEntityStateStats(private val _id: ObjectId, val totalEntities: Long)

fun EntityStateCollection.getStatistics(): AggregateFlow<UserEntityStateStats> = aggregate<UserEntityStateStats>(
	Aggregates.project(
		Projections.fields(
			Projections.excludeId(),
			Projections.computed(UserEntityStateStats::totalEntities.name, Aggregations.size(NodeEntityStates::entities))
		)
	),
	Aggregates.group(
		null,
		Accumulators.sum(UserEntityStateStats::totalEntities.name, UserEntityStateStats::totalEntities.mongoField),
	)
)
