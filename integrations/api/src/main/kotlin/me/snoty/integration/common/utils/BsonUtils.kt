package me.snoty.integration.common.utils

import me.snoty.integration.common.diff.Change
import me.snoty.integration.common.diff.ChangeCodec
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

fun integrationsApiCodecModule() =
	CodecRegistries.fromProviders(object : CodecProvider {
		override fun <T : Any> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? {
			return if (clazz == Change::class.java) ChangeCodec(registry) as Codec<T>
			else null
		}
	})
