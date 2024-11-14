package me.snoty.integration.builtin.mapper

import liqp.TemplateParser
import me.snoty.integration.common.model.metadata.DisplayName
import org.bson.Document
import org.slf4j.Logger

enum class MapperEngine(private val templater: Templater) {
	@DisplayName("Replace")
	REPLACE({ settings, data ->
		val mappedData = Document()
		settings.fields.forEach { (key, ogValue) ->
			var result = ogValue
			for (field in data) {
				result = result.replace("%${field.key}%", field.value.toString())
			}

			mappedData.setRecursively(key, result)
		}

		mappedData
	}),
	@DisplayName("Liquid")
	LIQUID({ settings, data ->
		val mappedData = Document()
		settings.fields.forEach { (key, value) ->
			val template = TemplateParser.DEFAULT.parse(value)
			val rendered = template.render(data as Map<String, Any>)

			mappedData.setRecursively(key, rendered)
		}

		mappedData
	});

	fun template(logger: Logger, settings: MapperSettings, data: Document): Document {
		val mappedData = templater(settings, data)

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

private fun Document.setRecursively(key: String, value: Any) {
	val parts = key.split(".")
	parts.dropLast(1).fold(this) { acc, part ->
		val next = acc[part] as? Document ?: Document()
		acc[part] = next
		next
	}[parts.last()] = value
}
