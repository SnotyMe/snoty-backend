package me.snoty.backend.database.mongo

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.AggregateFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import org.bson.Document
import org.bson.conversions.Bson

inline fun <reified T : Any> MongoCollection<*>.aggregate(vararg stages: Bson): AggregateFlow<T>
	= aggregate<T>(stages.toList())

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
