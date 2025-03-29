package me.snoty.integration.builtin.mapper.filter

import liqp.TemplateContext
import liqp.filters.Filter
import me.snoty.backend.utils.orNull
import me.snoty.integration.builtin.diff.uni.computeDiff

class UniDiffFilter : Filter("unidiff") {
	override fun apply(value: Any?, context: TemplateContext, vararg params: Any): Any? {
		val old = asString(value, context).orNull()
		if (params.isEmpty()) {
			throw IllegalArgumentException("UniDiff filter requires the new value as a parameter")
		}

		val new = asString(params[0], context).orNull()

		return computeDiff(old, new)
	}
}
