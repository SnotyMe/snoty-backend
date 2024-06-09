package me.snoty.backend.database.mongo

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.UpdateOptions
import com.mongodb.kotlin.client.coroutine.AggregateFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document
import org.bson.conversions.Bson

inline fun <reified T : Any> MongoCollection<*>.aggregate(vararg stages: Bson): AggregateFlow<T>
	= aggregate<T>(stages.toList())

suspend inline fun <reified T : Any> MongoCollection<*>.getByIdFromArray(path: String, id: Any): T? {
	// computed path that tells mongodb to use that value
	val computedPath = "\$$path"
	val configFilter = Filters.eq("$path.id", id)
	return aggregate<T>(
		Aggregates.match(configFilter),
		Aggregates.unwind(computedPath),
		Aggregates.match(configFilter),
		Aggregates.replaceRoot(computedPath)
	).firstOrNull()
}

suspend fun <R : Any> MongoCollection<R>.upsertOne(filter: Bson, update: Bson)
	= updateOne(filter, update, UpdateOptions().upsert(true))

object Aggregations {
	fun project(vararg projections: Bson): Bson
		= Aggregates.project(
			Projections.fields(
				*projections
			)
		)
}

object Stages {
	fun objectToArray(name: String): Bson
		= Document("\$objectToArray", name)
}

object Accumulations {
	fun mergeObjects(vararg objects: Any): Bson
		= Document("\$mergeObjects", objects.toList())
}
