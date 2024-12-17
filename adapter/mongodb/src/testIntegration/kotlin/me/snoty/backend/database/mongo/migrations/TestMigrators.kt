package me.snoty.backend.database.mongo.migrations

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.single
import org.bson.Document
import kotlin.test.assertEquals

data class OldTestObj(
	val type: String,
)

data class NewTestObj(
	val name: String,
)

fun migrators(collection: MongoCollection<Document>, failIn: String? = null, failWith: Throwable? = null): List<MongoMigration> {
	val logger = KotlinLogging.logger {}
	fun failIfNecessary(name: String) {
		logger.debug { "Failing because the name matches" }
		if (name == failIn) throw failWith!!
	}

	suspend fun MongoMigration.doExecute(expectedBefore: String, newType: String) {
		assertEquals(1, collection.countDocuments())
		assertEquals(expectedBefore, collection.find().single()["type"])

		collection.updateMany(Filters.empty(), Updates.combine(Updates.set("type", newType), Updates.push("visited", name)))

		failIfNecessary(name)
	}

	suspend fun MongoMigration.doRollback(expectedBefore: String, newType: String) {
		assertEquals(1, collection.countDocuments())
		assertEquals(expectedBefore, collection.find().single()["type"])

		logger.debug { "Rolling back $expectedBefore to $newType" }
		collection.updateMany(Filters.empty(), Updates.combine(Updates.set("type", newType), Updates.pull("visited", name)))
	}

	return listOf(
		object : MongoMigration("0.0.1") {
			override val name = "FirstMigration"

			override suspend fun execute(database: MongoDatabase) = doExecute("test", "first")

			override suspend fun rollback(database: MongoDatabase) {
				doRollback("first", "test")

				database.getCollection<OldTestObj>("migrationorder")
					.updateMany(Filters.empty(), Updates.combine(Updates.set("type", "test"), Updates.unset("visited")))
			}
		},
		object : MongoMigration("0.0.2") {
			override val name = "SecondMigration"
			override val namespace = "me.snoty.backend.database.mongo.migrations.impl"
			override suspend fun execute(database: MongoDatabase) = doExecute("first", "second")
			override suspend fun rollback(database: MongoDatabase) = doRollback("second", "first")
		},
		object : MongoMigration("0.0.2") {
			override val name = "SecondByThirdParty"
			override val namespace = "com.example.thirdparty"
			override suspend fun execute(database: MongoDatabase) = doExecute("second", "third")
			override suspend fun rollback(database: MongoDatabase) = doRollback("third", "second")
		},
		object : MongoMigration("0.0.3") {
			override val name = "ThirdMigration"
			override suspend fun execute(database: MongoDatabase) = doExecute("third", "fourth")
			override suspend fun rollback(database: MongoDatabase) = doRollback("fourth", "third")
		},
	)
}
