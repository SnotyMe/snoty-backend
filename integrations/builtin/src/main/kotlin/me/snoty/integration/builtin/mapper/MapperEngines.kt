package me.snoty.integration.builtin.mapper

import liqp.TemplateParser
import org.bson.Document

object MapperEngines {
	private val engines = mutableMapOf<String, Templater>()

	private fun engine(name: String, templater: Templater) {
		engines[name] = templater
	}

	init {
		engine("replace") { settings, data ->
			val mappedData = Document()
			settings.fields.forEach { (key, value) ->
				var result = value
				for (field in data) {
					result = result.replace("%${field.key}%", field.value.toString())
				}
				mappedData[key] = result
			}

			mappedData
		}

		engine("liquid") { settings, data ->
			val mappedData = Document()
			settings.fields.forEach { (key, value) ->
				val template = TemplateParser.DEFAULT.parse(value)
				val rendered = template.render(data as Map<String, Any>)

				mappedData[key] = rendered
			}

			mappedData
		}
	}

	fun get(settings: MapperSettings): Templater? {
		return engines[settings.engine]
	}
}
