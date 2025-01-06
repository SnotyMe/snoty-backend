package me.snoty.integration.common.utils

import kotlinx.datetime.Instant
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import me.snoty.integration.common.diff.Change
import me.snoty.integration.common.diff.ChangeCodec
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.common.diff.DiffResultCodec
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.EmptyNodeSettingsCodec
import me.snoty.integration.common.wiring.node.InvalidNodeSettings
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.BsonType
import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

@Suppress("UNCHECKED_CAST")
fun integrationsApiCodecModule(bsonTypeClassMap: BsonTypeClassMap): CodecRegistry =
	CodecRegistries.fromProviders(object : CodecProvider {
		override fun <T : Any> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? =
			when (clazz) {
				Change::class.java -> ChangeCodec(registry, bsonTypeClassMap)
				EmptyNodeSettings::class.java -> EmptyNodeSettingsCodec
				else -> null
			} as? Codec<T> ?: when {
				DiffResult::class.java.isAssignableFrom(clazz) -> DiffResultCodec(registry, bsonTypeClassMap)
				else -> null
			} as? Codec<T>
	})

val kotlinxSerializersModule = SerializersModule {
	polymorphic(NodeSettings::class) {
		subclass(EmptyNodeSettings::class)
		subclass(InvalidNodeSettings::class)
	}
}

@Single
fun bsonTypeClassMap() = BsonTypeClassMap(mapOf(
	BsonType.DATE_TIME to Instant::class.java,
))
