package me.snoty.backend.utils.bson

import org.bson.Document

private val UNESCAPED_DOT_REGEX = """(?<!\\)\.""".toRegex()
private val ESCAPE_REGEX = """\\(.)""".toRegex()
private val ARRAY_INDEX_REGEX = """^.*(?:[^\\]|\\\\)\[\d+]$""".toRegex()

fun Document.setByPath(key: String, value: Any?) {
	val parts = key.split(UNESCAPED_DOT_REGEX)
	if (parts.isEmpty()) throw IllegalArgumentException("Key must not be empty")

	var current: Any = this

	for (i in parts.indices) {
		// process escapes - `\[` => `[`
		val part = parts[i].replace(ESCAPE_REGEX, "$1")
		val isLast = i == parts.lastIndex
		current = when {
			// use parts[i] to avoid double escape (`\[` in the part would be replaced by just `[`, resulting in a false positive)
			// however, `\\[` should match too, therefore, `\\` is an alternative to the `[^\\]`
			parts[i].matches(ARRAY_INDEX_REGEX) -> current.handleList(part, isLast, value)
			else -> current.handleRegularKey(part, isLast, value)
		}
	}
}

private fun Any.handleRegularKey(part: String, isLast: Boolean, value: Any?): Any {
	if (this !is Document) {
		throw IllegalArgumentException("Expected a Document at '$part', but found: ${this::class.simpleName}")
	}

	return if (isLast) {
		this[part] = value
		this
	} else {
		this.getOrPut(part) { Document() }
	}
}

private fun Any.handleList(part: String, isLast: Boolean, value: Any?): Any {
	val key = part.substringBeforeLast("[")
	val index = part.substringAfterLast("[").substringBefore("]").toInt()

	@Suppress("UNCHECKED_CAST")
	val list = when (this) {
		is Document -> this.getOrPut(key) { mutableListOf<Any?>() }
		is MutableList<*> -> this
		else -> throw IllegalArgumentException("Expected a Document or List at '$part', but found: ${this::class.simpleName}")
	} as MutableList<Any?>

	while (index >= list.size) {
		list.add(null)
	}

	return if (isLast) {
		list[index] = value
		list
	} else {
		when {
			list[index] == null -> list[index] = Document()
			list[index] != null && list[index] !is Document ->
				throw IllegalArgumentException("Expected a Document at '$part[$index]', but found: ${list[index]!!::class.simpleName}")
		}

		list[index]!!
	}
}
