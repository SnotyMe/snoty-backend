package me.snoty.backend.database.mongo.migrations

import com.github.zafarkhaja.semver.Version
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.toList
import me.snoty.backend.utils.errors.CaughtException
import me.snoty.backend.utils.errors.causeIfCaught
import me.snoty.backend.utils.errors.nullIfCaught
import org.bson.types.ObjectId
import org.koin.core.annotation.Property
import org.koin.core.annotation.PropertyValue
import org.koin.core.annotation.Single
import kotlin.time.Clock

@PropertyValue(MongoMigrator.COLLECTION_PROPERTY_NAME)
const val DEFAULT_MIGRATION_COLLECTION_NAME = "migrations"

@Single
class MongoMigrator(
	private val mongoDatabase: MongoDatabase,
	private val migrations: List<MongoMigration>,
	@Property(COLLECTION_PROPERTY_NAME) collectionName: String = DEFAULT_MIGRATION_COLLECTION_NAME,
) {
	companion object {
		private val logger = KotlinLogging.logger {}
		const val COLLECTION_PROPERTY_NAME = "migrations.collection.name"
	}

	private val collection = mongoDatabase.getCollection<MongoMigrationData>(collectionName)

	suspend fun migrate() {
		val alreadyRan = collection.find().toList()
		logger.trace { "Migrations that ran before: $alreadyRan" }

		val toRun = migrations.filterNot {
			val didFinish = alreadyRan
				.filter { it.events.any { it is MongoMigrationEvent.Completed } }
				.any { ran -> ran.name == it.name }

			logger.debug { "Migration ${it.name} did finish before: $didFinish" }
			didFinish
		}.sortedWith(
			compareBy(
				{ Version.parse(it.appVersion) },
				{ it.name.startsWith(this@MongoMigrator.javaClass.packageName) },
			)
		)
		logger.debug { "Migrations to run: $toRun" }

		val executed: LinkedHashMap<ObjectId, MongoMigration> = LinkedHashMap()
		try {
			toRun.runMigrationSteps(executed)
		} catch (e: Exception) {
			logger.error(e.nullIfCaught()) { "Migrating MongoDB failed. Consult the debug and trace logs for more information." }

			// Rollback the executed migrations
			try {
				executed
					.reversed()
					.rollback()
			} catch (rollbackException: Exception) {
				logger.error(rollbackException.nullIfCaught()) { "Rollback of executed migrations failed. Well, you're probably screwed. I sure hope you have a backup :^)" }
				val finalException = runCatching { rollbackException.initCause(e) }.getOrElse { e }
				throw MigrationFailedException("Rollback failed", finalException.causeIfCaught())
			}

			logger.info { "Rollback succeeded. There's a fair chance fixing the initial issues will result in a working migration." }

			// Rollback was successful, rethrow the original exception
			throw MigrationFailedException("Migration failed", e.causeIfCaught())
		}
	}

	private suspend fun List<MongoMigration>.runMigrationSteps(executed: MutableMap<ObjectId, MongoMigration>) = forEach { migration ->
		logger.info {
			"Running Migration ${migration.name}" + if (migration.description != null) ": ${migration.description}" else ""
		}
		val migrationId = ObjectId()
		collection.insertOne(MongoMigrationData(
			_id = migrationId,
			namespace = migration.namespace,
			name = migration.name,
			createdAt = Clock.System.now(),
			events = listOf(MongoMigrationEvent.Running()),
		))
		// we'll add it so it can rollback even if it fails
		executed += migrationId to migration

		try {
			migration.execute(mongoDatabase)
		} catch (e: Exception) {
			logger.error(e) { "Migration ${migration.name} failed!" }
			addEvent(migrationId, MongoMigrationEvent.Failed(exceptionMessage = e.message ?: "No message"))
			throw CaughtException(e)
		}

		addEvent(migrationId, MongoMigrationEvent.Completed())
		logger.info { "Migration ${migration.name} completed" }
	}

	private suspend fun Map<ObjectId, MongoMigration>.rollback() = forEach { (migrationId, migration) ->
		logger.info { "Rolling back Migration ${migration.name}" }

		try {
			migration.rollback(mongoDatabase)
			addEvent(migrationId, MongoMigrationEvent.RolledBack())
		} catch (e: Exception) {
			logger.error(e) { "Rollback of Migration ${migration.name} failed!" }
			addEvent(migrationId, MongoMigrationEvent.RollbackFailed(exceptionMessage = e.message ?: "No message"))
			throw CaughtException(e)
		}

		logger.debug { "Rollback of Migration ${migration.name} finished" }
	}

	private suspend fun addEvent(migrationId: ObjectId, event: MongoMigrationEvent) {
		collection.updateOne(Filters.eq(MongoMigrationData::_id.name, migrationId), Updates.push(MongoMigrationData::events.name, event))
	}
}
