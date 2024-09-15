package me.snoty.integration.common.utils

import kotlinx.datetime.Instant
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import me.snoty.backend.integration.config.flow.NodeIdSerializer
import me.snoty.integration.common.diff.Change
import me.snoty.integration.common.diff.ChangeCodec
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.common.diff.DiffResultCodec
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.EmptyNodeSettingsCodec
import org.bson.BsonType
import org.bson.Document
import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.Codec
import org.bson.codecs.DocumentCodec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

@Suppress("UNCHECKED_CAST")
fun integrationsApiCodecModule(): CodecRegistry =
	CodecRegistries.fromProviders(object : CodecProvider {
		private val bsonTypeClassMap = BsonTypeClassMap(mapOf(
			BsonType.DATE_TIME to Instant::class.java,
		))

		override fun <T : Any> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? {
			return when (clazz) {
				Document::class.java -> DocumentCodec(registry, bsonTypeClassMap)
				Change::class.java -> ChangeCodec(registry, bsonTypeClassMap)
				DiffResult::class.java -> DiffResultCodec(registry)
				EmptyNodeSettings::class.java -> EmptyNodeSettingsCodec
				else -> null
			} as? Codec<T>
		}
	})

val kotlinxSerializersModule = SerializersModule {
	contextual(NodeIdSerializer)
}
