package me.snoty.integration.builtin.mapper

import liqp.TemplateParser
import me.snoty.integration.common.model.metadata.DisplayName
import org.bson.Document

enum class MapperEngine(val templater: Templater) {
	@DisplayName("Replace")
	REPLACE({ settings, data ->
		val mappedData = settings.fields.mapValues {
			var result = it.value
			for (field in data) {
				result = result.replace("%${field.key}%", field.value.toString())
			}
			result
		}

		Document(mappedData)
	}),
	@DisplayName("Liquid")
	LIQUID({ settings, data ->
		val mappedData = Document()
		settings.fields.forEach { (key, value) ->
			val template = TemplateParser.DEFAULT.parse(value)
			val rendered = template.render(data as Map<String, Any>)

			mappedData[key] = rendered
		}

		mappedData
	})
}
