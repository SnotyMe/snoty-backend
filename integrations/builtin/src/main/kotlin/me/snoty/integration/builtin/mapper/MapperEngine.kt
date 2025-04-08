package me.snoty.integration.builtin.mapper

import liqp.TemplateParser
import me.snoty.backend.utils.bson.setRecursively
import me.snoty.integration.builtin.mapper.filter.FormatFilter
import me.snoty.integration.builtin.mapper.filter.UniDiffFilter
import me.snoty.integration.common.model.metadata.DisplayName
import org.bson.Document
import org.slf4j.Logger

enum class MapperEngine(private val templater: Templater) {
	@DisplayName("Replace")
	REPLACE({ logger, settings, data ->
		val mappedData = Document()
		settings.fields.forEach { (key, ogValue) ->
			var result = ogValue
			for (field in data) {
				result = result.replace("%${field.key}%", field.value.toString())
			}

			mappedData.setRecursively(key, result.trim())
		}

		mappedData
	}),

	@DisplayName("Liquid")
	LIQUID({ logger, settings, data ->
		val mappedData = Document()
		val templateParser = TemplateParser.Builder()
			.withFilter(UniDiffFilter())
			.withFilter(FormatFilter(logger))
			.build()
		settings.fields.forEach { (key, value) ->
			val template = templateParser.parse(value)
			val rendered = template.render(data).trim()

			mappedData.setRecursively(key, rendered)
		}

		mappedData
	});

	fun template(logger: Logger, settings: MapperSettings, data: Document): Document {
		val mappedData = templater(logger, settings, data)

		if (settings.preserveId) {
			val alreadyMapped = mappedData["id"] != null
			val inputHasId = data["id"] != null
			when {
				!alreadyMapped && inputHasId -> mappedData["id"] = data["id"].toString()
				alreadyMapped -> logger.warn("Configured to preserve ID, but output already contains an ID")
				else -> logger.warn("Configured to preserve ID, but input does not contain an ID")
			}
		}

		return mappedData
	}
}
