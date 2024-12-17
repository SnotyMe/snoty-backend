package me.snoty.backend.database.mongo.migrations

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.snoty.backend.database.mongo.MongoTest
import org.bson.Document
import org.bson.codecs.configuration.CodecConfigurationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class MongoMigrationTest {
	val mongoDB = MongoTest.getMongoDatabase {}

	@Test
	fun testSimpleMigration() = runBlocking {
		val oldCollection = mongoDB.getCollection<OldTestObj>("testentities")
		oldCollection.insertOne(OldTestObj("test"))

		val migration = object : MongoMigration("0.0.1") {
			override val name: String = "TestMigration"

			override suspend fun execute(database: MongoDatabase) {
				val collection = database.getCollection<NewTestObj>("testentities")
				collection.updateMany(Filters.empty(), Updates.rename("type", "name"))
			}

			override suspend fun rollback(database: MongoDatabase) = throw NotImplementedError()
		}

		MongoMigrator(mongoDB, listOf(migration), "simple:migrations").migrate()

		val newCollection = mongoDB.getCollection<NewTestObj>("testentities")
		assertEquals(1, oldCollection.countDocuments())
		assertEquals(1, newCollection.countDocuments())

		// collection has been migrated, doesn't conform to the old schema anymore
		assertThrows<CodecConfigurationException> { oldCollection.find().toList() }

		val elements = newCollection.find().toList()
		assertEquals("test", elements[0].name)
	}

	@Test
	fun testMigrationOrdering() = runBlocking {
		val collection = mongoDB.getCollection<OldTestObj>("migrationorder")
		collection.insertOne(OldTestObj("test"))

		MongoMigrator(mongoDB, migrators(mongoDB.getCollection("migrationorder")), "migrationorder:migrations").migrate()

		val elements = collection.find().toList()
		assertEquals(1, elements.size)
		assertEquals("fourth", elements[0].type)
		val elementsDoc = collection.find<Document>()
		assertEquals(listOf("FirstMigration", "SecondMigration", "SecondByThirdParty", "ThirdMigration"), elementsDoc.single()["visited"])
	}

	@Test
	fun testRollbacks() = runBlocking {
		suspend fun prepareCollection(name: String): MongoCollection<Document> {
			val collection = mongoDB.getCollection<Document>("rollback:$name")
			collection.insertOne(Document("type", "test"))
			return collection
		}

		val migrators = listOf("FirstMigration", "SecondMigration", "SecondByThirdParty", "ThirdMigration")
		for (elementFailing in migrators) {
			val collection = prepareCollection(elementFailing)

			val migratorWorking = MongoMigrator(mongoDB, migrators(collection), "rollback:migrations:$elementFailing")
			migratorWorking.migrate()

			assertEquals(1, collection.countDocuments())
			assertEquals(migrators, collection.find().single()["visited"])
			assertEquals("fourth", collection.find().single()["type"])
			val docs = collection.find().toList()

			val expectedEx = RuntimeException("Rollback failed")
			val migratorWithFailPotential = MongoMigrator(mongoDB, migrators(collection, failIn = elementFailing, failWith = expectedEx), "rollback:migrations:$elementFailing")
			// the migrations already ran, so this should not throw
			assertDoesNotThrow { migratorWithFailPotential.migrate() }
			assertEquals(docs, collection.find().toList())

			val failCollection = prepareCollection("$elementFailing:fail")
			val migratorFailing = MongoMigrator(mongoDB, migrators(failCollection, failIn = elementFailing, failWith = expectedEx), "rollback:migrations:$elementFailing:fail")
			val ex = assertThrows<MigrationFailedException> { migratorFailing.migrate() }
			assertEquals(expectedEx, ex.cause)

			// should've rolled back correctly
			assertEquals(1, failCollection.countDocuments())
			assertEquals(emptyList<String>(), failCollection.find().single()["visited"])
			assertEquals("test", failCollection.find().single()["type"])
		}
	}
}
