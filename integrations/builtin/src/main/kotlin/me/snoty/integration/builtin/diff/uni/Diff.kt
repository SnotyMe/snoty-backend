package me.snoty.integration.builtin.diff.uni

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import me.snoty.backend.utils.skip

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
