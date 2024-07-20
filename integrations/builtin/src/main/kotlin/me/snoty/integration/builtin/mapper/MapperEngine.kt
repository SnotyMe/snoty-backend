package me.snoty.integration.builtin.mapper

import liqp.TemplateParser
import me.snoty.integration.common.model.metadata.FieldName
import org.bson.Document

enum class MapperEngine(val templater: Templater) {
	@FieldName("Replace")
	REPLACE({ settings, data ->
		val mappedData = Document()
		settings.fields.forEach { (key, value) ->
			var result = value
			for (field in data) {
				result = result.replace("%${field.key}%", field.value.toString())
			}
			mappedData[key] = result
		}

		mappedData
	}),
	@FieldName("Liquid")
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
