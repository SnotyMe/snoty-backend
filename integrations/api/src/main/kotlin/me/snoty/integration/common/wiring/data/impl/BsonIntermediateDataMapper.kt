package me.snoty.integration.common.wiring.data.impl

import me.snoty.backend.database.mongo.decode
import me.snoty.backend.database.mongo.encode
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.IntermediateDataMapper
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

data class BsonIntermediateData(override val value: Document) : IntermediateData

@Single
class BsonIntermediateDataMapper(private val codecRegistry: CodecRegistry) : IntermediateDataMapper<BsonIntermediateData> {
	override val intermediateDataClass = BsonIntermediateData::class

	override fun <R : Any> deserialize(intermediateData: BsonIntermediateData, clazz: KClass<R>): R {
		if (clazz == Document::class) {
			// we've verified that `R` is `Document` thanks to the class parameter
			@Suppress("UNCHECKED_CAST")
			return intermediateData.value as R
		}

		return codecRegistry.decode(clazz, intermediateData.value)
	}

	override fun <R : Any> serialize(data: R): BsonIntermediateData {
		return BsonIntermediateData(codecRegistry.encode(data))
	}
}
