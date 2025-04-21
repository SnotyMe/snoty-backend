package me.snoty.integration.common.wiring.data.impl

import me.snoty.backend.utils.bson.decode
import me.snoty.backend.utils.bson.encode
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.IntermediateDataMapper
import org.bson.Document
import org.bson.codecs.DocumentCodec
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

data class BsonIntermediateData(override val value: Document, val documentCodec: DocumentCodec) : IntermediateData {
	override fun toString(): String = value.toJson(documentCodec)
}

@Single
class BsonIntermediateDataMapper(private val codecRegistry: CodecRegistry, private val documentCodec: DocumentCodec) : IntermediateDataMapper<BsonIntermediateData> {
	override val intermediateDataClass = BsonIntermediateData::class

	override fun <R : Any> deserialize(intermediateData: BsonIntermediateData, clazz: KClass<R>): R {
		if (clazz == Document::class) {
			// we've verified that `R` is `Document` thanks to the class parameter
			@Suppress("UNCHECKED_CAST")
			return intermediateData.value as R
		}

		return codecRegistry.decode(clazz, intermediateData.value)
	}

	override fun <R : Any> serialize(data: R) = BsonIntermediateData(
		when (data) {
			is Document -> data
			else -> codecRegistry.encode(data)
		},
		documentCodec
	)
}
