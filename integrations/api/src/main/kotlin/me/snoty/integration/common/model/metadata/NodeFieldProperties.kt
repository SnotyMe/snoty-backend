package me.snoty.integration.common.model.metadata

import org.jetbrains.annotations.Range

annotation class FieldHidden
annotation class FieldCensored
annotation class FieldName(val value: String)
annotation class FieldDescription(val value: String)

/**
 * @param values The number of lines to display in the editor
 */
annotation class Multiline(
	val values: @Range(from = 1, to = Int.MAX_VALUE.toLong()) Int = DEFAULT_MULTILINE_LINES,
) {
	companion object {
		const val DEFAULT_MULTILINE_LINES = 3
		const val DEFAULT_LINES = 1
	}
}
