package me.snoty.backend.database.mongo

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.AggregateFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import org.bson.Document
import org.bson.conversions.Bson
import kotlin.reflect.KClass

inline fun <reified T : Any> MongoCollection<*>.aggregate(vararg stages: Bson): AggregateFlow<T>
	= aggregate<T>(stages.toList())

fun <R : Any> MongoCollection<*>.aggregate(resultClass: KClass<R>, vararg stages: Bson)
	= aggregate(stages.toList(), resultClass.java)

object Aggregations {
	fun project(vararg projections: Bson): Bson
		= Aggregates.project(
			Projections.fields(
				*projections
			)
		)
}

object Stages {
	fun objectToArray(name: String): Document {
		return Document("\$objectToArray", name)
	}
}
