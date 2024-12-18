package me.snoty.backend.database.mongo.migrations

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.ClientSession
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import org.koin.core.annotation.Single

@Single
class MongoMigrator(
	private val mongoClient: MongoClient,
	private val mongoDatabase: MongoDatabase,
	private val migrations: List<MongoMigration>,
) {
	private val logger = KotlinLogging.logger {}

	private val collection = mongoDatabase.getCollection<MongoMigrationData>("migrations")

	suspend fun migrate() {
		val session = mongoClient.startSession()
		session.startTransaction()

		try {
			runMigrationSteps(session)
			session.commitTransaction()
		} catch (e: Exception) {
			logger.error(e) { "Migrating MongoDB failed. Consult the debug and trace logs for more information." }
			session.abortTransaction()
			throw e
		}
	}

	private suspend fun runMigrationSteps(session: ClientSession) {
		val alreadyRan = collection.find().toList()
		logger.trace { "Migrations that ran before: $alreadyRan" }

		migrations.filterNot {
			val didRun = alreadyRan.any { ran -> ran.name == it.name }
			logger.debug { "Migration ${it.name} did run: $didRun" }
			didRun
		}.forEach { migration ->
			logger.info {
				"Running Migration ${migration.name}" + if (migration.description != null) ": ${migration.description}" else ""
			}
			collection.insertOne(session, MongoMigrationData(migration.name, System.now()))

			try {
				migration.runMigration(session, mongoDatabase)
			} catch (e: Exception) {
				logger.error(e) { "Migration ${migration.name} failed!" }
				throw e
			}
			logger.debug { "Migration ${migration.name} finished running, persisting to DB..." }

			collection.updateOne(
				session,
				Filters.eq(MongoMigrationData::name.name, migration.name),
				Updates.set(MongoMigrationData::completedAt.name, System.now())
			)

			logger.info { "Migration ${migration.name} completed" }
		}
	}
}

data class MongoMigrationData(
	val name: String,
	val startedAt: Instant,
	val completedAt: Instant? = null,
)
