package me.snoty.backend.utils

/**
 * Converts lowerPascalCase or SNAKE_CASE to Title Case
 */
fun String.toTitleCase() =
	replace(Regex("([a-z])([A-Z\\d])|(?<=\\d)([A-Z])")) { "${it.groupValues[1]} ${it.groupValues[2]}${it.groupValues[3]}" }
	.replace(Regex("[_-]"), " ")
	.split(Regex("\\s+"))
	.joinToString(" ") { word ->
		word.lowercase().replaceFirstChar { it.titlecase() }
	}

/**
 * @return this string if it is not blank, otherwise null
 */
fun String?.orNull() = this?.ifBlank { null }

fun String.quoted() = "\"$this\""
fun String.unquoted() = removeSurrounding("\"")
