package me.snoty.backend.database.mongo

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Projections
import com.mongodb.client.model.UpdateOptions
import com.mongodb.kotlin.client.coroutine.AggregateFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import org.bson.Document
import org.bson.conversions.Bson
import kotlin.reflect.KProperty

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

	fun unwind(field: KProperty<*>): Bson
		= Aggregates.unwind("$${field.name}")
}

object Stages {
	fun objectToArray(name: String): Bson
		= Document("\$objectToArray", "$$name")
}

object Accumulations {
	fun mergeObjects(vararg objects: Any): Bson
		= Document("\$mergeObjects", objects.toList())
}
