package me.snoty.backend.database.mongo.tracing

import io.opentelemetry.api.common.AttributeKey

val DATABASE_NAME = AttributeKey.stringKey("db.namespace")!!
val COLLECTION_NAME = AttributeKey.stringKey("db.collection.name")!!
val OPERATION_NAME = AttributeKey.stringKey("db.operation.name")!!
val QUERY_TEXT = AttributeKey.stringKey("db.query.text")!!
