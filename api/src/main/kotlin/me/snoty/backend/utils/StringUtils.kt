package me.snoty.backend.utils

/**
 * Converts lowerPascalCase to Title Case
 */
fun String.toTitleCase()
	= replaceFirstChar { it.uppercaseChar() }
		.replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]} ${it.groupValues[2]}" }

/**
 * @return this string if it is not blank, otherwise null
 */
fun String?.orNull() = this?.ifBlank { null }
