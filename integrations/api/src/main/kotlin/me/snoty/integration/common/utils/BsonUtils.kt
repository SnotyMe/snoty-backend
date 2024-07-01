package me.snoty.integration.common.utils

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import me.snoty.backend.integration.config.flow.NodeIdSerializer
import me.snoty.integration.common.diff.Change
import me.snoty.integration.common.diff.ChangeCodec
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.common.diff.DiffResultCodec
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

@Suppress("UNCHECKED_CAST")
fun integrationsApiCodecModule(): CodecRegistry =
	CodecRegistries.fromProviders(object : CodecProvider {
		override fun <T : Any> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? {
			return when (clazz) {
				Change::class.java -> ChangeCodec(registry) as Codec<T>
				DiffResult::class.java -> DiffResultCodec(registry) as Codec<T>
				else -> null
			}
		}
	})

val kotlinxSerializersModule = SerializersModule {
	contextual(NodeIdSerializer)
}
