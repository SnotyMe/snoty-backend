package me.snoty.backend.utils

/**
 * Converts lowerPascalCase to Title Case
 */
fun String.toTitleCase()
	= replaceFirstChar { it.uppercaseChar() }
		.replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]} ${it.groupValues[2]}" }
