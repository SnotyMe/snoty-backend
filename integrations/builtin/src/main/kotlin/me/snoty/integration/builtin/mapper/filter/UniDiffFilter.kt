package me.snoty.integration.builtin.mapper.filter

import liqp.TemplateContext
import liqp.filters.Filter
import me.snoty.backend.utils.orNull
import me.snoty.integration.builtin.diff.uni.computeDiff
import me.snoty.integration.common.diff.getNew
import me.snoty.integration.common.diff.getOld
import org.bson.Document

class UniDiffFilter : Filter("unidiff") {
	override fun apply(value: Any?, context: TemplateContext, vararg params: Any): String {
		val (old, new) = when (value) {
			is Document -> {
				val old = value.getOld() ?: throw IllegalArgumentException("Diff must contain 'old' value")
				val new = value.getNew() ?: throw IllegalArgumentException("Diff must contain 'new' value")

				old to new
			}

			else -> {
				val old = asString(value, context).orNull()
				if (params.isEmpty()) {
					throw IllegalArgumentException("UniDiff filter requires the new value as a parameter")
				}
				val new = asString(params[0], context).orNull()

				old to new
			}
		}

		return computeDiff(old, new)
	}
}
