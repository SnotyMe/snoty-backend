package me.snoty.backend.database.mongo.migrations.impl

import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.mongodb.kotlin.client.model.Filters.exists
import com.mongodb.kotlin.client.model.Projections.projection
import me.snoty.backend.database.mongo.migrations.MongoMigration
import org.bson.Document
import org.koin.core.annotation.Single

@Single
class MigrateUserIdToString : MongoMigration("0.8.0") {
    override val name: String = "MigrateUserIdToString"
    override val description: String = "Migrate User IDs from UUID to String"

    data class HasUserId(val userId: String)

    private val migrateCollections = listOf("flows", "nodes")

    override suspend fun execute(database: MongoDatabase) {
        migrateCollections.forEach { collectionName ->
            database.getCollection<HasUserId>(collectionName)
                .updateMany(
                    filter = HasUserId::userId.exists(true),
                    // IMPORTANT: needs aggregation syntax (list)
                    update = listOf(
                        Updates.set(HasUserId::userId.name, Document($$"$toString", HasUserId::userId.projection))
                    )
                )
        }
    }

    override suspend fun rollback(database: MongoDatabase) {
        migrateCollections.forEach { collectionName ->
            database.getCollection<HasUserId>(collectionName)
                .updateMany(
                    filter = HasUserId::userId.exists(true),
                    // IMPORTANT: needs aggregation syntax (list)
                    update = listOf(
                        Updates.set(HasUserId::userId.name, Document($$"$toUUID", HasUserId::userId.projection))
                    )
                )
        }
    }
}
