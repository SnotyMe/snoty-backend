package me.snoty.integration.builtin.mapper.filter

import liqp.TemplateContext
import liqp.filters.Filter
import me.snoty.integration.builtin.mapper.FilterFactory
import me.snoty.integration.builtin.utils.parseJson
import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single
import org.koin.core.component.get
import org.slf4j.Logger

@Single
class FromJsonFilterFactory : FilterFactory {
	override fun createFilter(logger: Logger) =
		FromJsonFilter(get(), get())
}

class FromJsonFilter(
	private val codecRegistry: CodecRegistry,
	private val bsonTypeClassMap: BsonTypeClassMap,
) : Filter("from_json") {
	override fun apply(value: Any?, context: TemplateContext, vararg params: Any): Any? {
		if (value !is String) return value

		return value.parseJson(codecRegistry, bsonTypeClassMap)
	}
}
