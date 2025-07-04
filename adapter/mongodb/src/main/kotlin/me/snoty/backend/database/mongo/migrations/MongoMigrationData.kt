package me.snoty.backend.database.mongo.migrations

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.snoty.backend.utils.bson.CodecRegistryProvider
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.kotlinx.BsonConfiguration
import org.bson.codecs.kotlinx.KotlinSerializerCodec
import org.bson.types.ObjectId
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.reflect.full.isSubclassOf
import kotlin.time.Clock
import kotlin.time.Instant

data class MongoMigrationData(
	val _id: ObjectId,
	/**
	 * Java package name, used to ensure unique migration names.
	 */
	val namespace: String,
	val name: String,
	val createdAt: Instant,
	val events: List<MongoMigrationEvent>
)

@Serializable
sealed class MongoMigrationEvent(val timestamp: Instant = Clock.System.now()) {
	@Serializable @SerialName("Running")
	class Running : MongoMigrationEvent()

	@Serializable @SerialName("Completed")
	class Completed : MongoMigrationEvent()

	@Serializable @SerialName("Failed")
	data class Failed(val exceptionMessage: String) : MongoMigrationEvent()

	@Serializable @SerialName("RolledBack")
	class RolledBack : MongoMigrationEvent()
	@Serializable @SerialName("RollbackFailed")
	data class RollbackFailed(val exceptionMessage: String) : MongoMigrationEvent()
}


private val migrationEventCodec = KotlinSerializerCodec.create<MongoMigrationEvent>(
	bsonConfiguration = BsonConfiguration(
		encodeDefaults = true,
		classDiscriminator = "type",
	),
)

@Suppress("UNCHECKED_CAST")
@Single
@Named("migrationCodecProvider")
fun provideMigrationsCodec() = CodecRegistryProvider(
	CodecRegistries.fromProviders(
		object : CodecProvider {
			override fun <T : Any> get(clazz: Class<T>, registry: CodecRegistry?): Codec<T>? = when {
				clazz.kotlin.isSubclassOf(MongoMigrationEvent::class) -> migrationEventCodec
				else -> null
			} as? Codec<T>
		}
	),
	priority = 1000
)
