package me.snoty.integration.builtin.mapper

import liqp.TemplateParser
import me.snoty.backend.injection.getFromAllScopes
import me.snoty.backend.utils.bson.setByPath
import org.bson.Document
import org.koin.core.component.KoinComponent
import org.slf4j.Logger

enum class MapperEngine(private val templater: Templater) {
	REPLACE({ logger, settings, data ->
		val mappedData = Document()
		settings.fields.forEach { (key, ogValue) ->
			var result = ogValue
			for (field in data) {
				result = result.replace("%${field.key}%", field.value.toString())
			}

			mappedData.setByPath(key, result.trim())
		}

		mappedData
	}),

	LIQUID({ logger, settings, data ->
		val mappedData = Document()
		val templateParser = TemplateParser.Builder()
			.apply {
				getKoin()
					.getFromAllScopes<FilterFactory>()
					.forEach {
						withFilter(it.createFilter(logger))
					}
			}
			.build()

		settings.fields.forEach { (key, value) ->
			val template = templateParser.parse(value)
			val rendered = template.render(data).trim()

			mappedData.setByPath(key, rendered)
		}

		mappedData
	});

	fun template(logger: Logger, koinComponent: KoinComponent, settings: MapperSettings, data: Document): Document {
		val mappedData = koinComponent.templater(logger, settings, data)

		if (settings.preserveId) {
			val alreadyMapped = mappedData["id"] != null
			val inputHasId = data["id"] != null
			when {
				!alreadyMapped && inputHasId -> mappedData["id"] = data["id"].toString()
				alreadyMapped -> logger.warn("Configured to preserve ID, but output already contains an ID")
				else -> logger.warn("Configured to preserve ID, but input does not contain an ID")
			}
		}
		settings.preserveFields.forEach {
			if (data.containsKey(it)) {
				mappedData[it] = data[it]
			} else {
				logger.warn("Configured to preserve field '$it', but input does not contain this field")
			}
		}

		return mappedData
	}
}
