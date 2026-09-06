package me.snoty.integration.builtin.mapper.filter

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import liqp.TemplateContext
import liqp.filters.Filter
import me.snoty.backend.utils.orNull
import me.snoty.backend.utils.skip
import me.snoty.integration.builtin.mapper.FilterFactory
import me.snoty.integration.common.diff.getNew
import me.snoty.integration.common.diff.getOld
import org.bson.Document
import org.koin.core.annotation.Single
import org.slf4j.Logger

@Single
class UniDiffFilterFactory : FilterFactory {
	override fun createFilter(logger: Logger) = UniDiffFilter()
}

class UniDiffFilter : Filter("unidiff") {
	override fun apply(value: Any?, context: TemplateContext, vararg params: Any): String {
		val (old, new) = when (value) {
			is Document -> {
				val old = value.getOld() ?: throw IllegalArgumentException("Diff must contain 'old' value")
				val new = value.getNew() ?: throw IllegalArgumentException("Diff must contain 'new' value")

				old to new
			}

			is String -> {
				val old = asString(value, context).orNull()
				if (params.isEmpty()) {
					throw IllegalArgumentException("UniDiff filter requires the new value as a parameter")
				}
				val new = asString(params[0], context).orNull()

				old to new
			}

			else -> throw IllegalArgumentException("UniDiff filter requires a Document or String as input, got ${value?.javaClass?.name ?: "null"}")
		}

		return computeDiff(old, new)
	}
}

fun computeDiff(old: String?, new: String?): String {
	fun String?.toList() = this?.split("\n")
		?.ifEmpty { emptyList() }
		?: emptyList()

	val old = old.toList()
	val diff = DiffUtils.diff(old, new.toList())

	val unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
		/* originalFileName = */ "old",
		/* revisedFileName = */ "new",
		/* originalLines = */ old,
		/* patch = */ diff,
		/* contextSize = */ 2
	)

	// skip the `--- old` and `+++ new` lines
	return unifiedDiff.skip(2).joinToString(separator = "\n")
}
