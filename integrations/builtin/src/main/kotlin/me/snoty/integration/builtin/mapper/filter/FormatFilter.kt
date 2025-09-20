package me.snoty.integration.builtin.mapper.filter

import liqp.TemplateContext
import liqp.filters.Filter
import me.snoty.integration.builtin.mapper.FilterFactory
import org.koin.core.annotation.Single
import org.slf4j.Logger

@Single
class FormatFilterFactory : FilterFactory {
	override fun createFilter(logger: Logger) = FormatFilter(logger)
}

class FormatFilter(private val logger: Logger) : Filter("format") {
	override fun apply(value: Any?, context: TemplateContext, vararg params: Any): Any? {
		if (value == null) {
			logger.debug("Format filter got a null value, returning null")
			return value
		}

		val format = params[0]
		if (params.isEmpty() || format !is String) {
			throw IllegalArgumentException("Format filter requires a format as a string parameter")
		}

		return when (params.size) {
			1 -> format.format(value)
			else -> throw IllegalArgumentException("Format filter requires exactly one argument")
		}
	}
}
