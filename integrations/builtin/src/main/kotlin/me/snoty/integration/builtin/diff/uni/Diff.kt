package me.snoty.integration.builtin.diff.uni

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import me.snoty.integration.common.diff.Change

fun computeDiff(change: Change<String, String>): String {
	fun String?.toList() = (this ?: "null").split("\n")

	val old = change.old.toList()
	val diff = DiffUtils.diff(old, change.new.toList())

	val unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
		/* originalFileName = */ "old",
		/* revisedFileName = */ "new",
		/* originalLines = */ old,
		/* patch = */ diff,
		/* contextSize = */ 2
	)

	return unifiedDiff.joinToString(separator = "\n")
}
