package me.snoty.backend.database.mongo

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Projections
import com.mongodb.client.model.UpdateOptions
import com.mongodb.kotlin.client.coroutine.AggregateFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.model.Projections.projection
import me.snoty.backend.errors.InvalidIdException
import me.snoty.backend.wiring.node.MongoNode
import me.snoty.backend.wiring.node.NodeSettingsDeserializationService
import me.snoty.core.FlowId
import me.snoty.core.NodeId
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import kotlin.reflect.KProperty

val FlowId.objectId get() = this.value.objectId
val NodeId.objectId get() = this.value.objectId

val String.objectId get() = try {
	ObjectId(this)
} catch (e: IllegalArgumentException) {
	throw InvalidIdException(e)
}

fun ObjectId.toFlowId() = FlowId(this.toHexString())
fun ObjectId.toNodeId() = NodeId(this.toHexString())

inline fun <reified T : Any> MongoCollection<*>.aggregate(vararg stages: Bson): AggregateFlow<T>
	= aggregate<T>(stages.toList())

suspend fun <R : Any> MongoCollection<R>.upsertOne(filter: Bson, update: Bson)
	= updateOne(filter, update, UpdateOptions().upsert(true))

object Aggregations {
	fun project(vararg projections: Bson): Bson
		= Aggregates.project(
			Projections.fields(
				*projections
			)
		)

	fun size(field: KProperty<*>): Bson
		= Document($$"$size", field.projection)
}

object Stages {
	fun objectToArray(name: String): Bson
		= Document($$"$objectToArray", name.projection)
}

fun NodeSettingsDeserializationService.deserializeOrInvalid(node: MongoNode) =
	deserializeOrInvalid(node.descriptor, node.settings)
