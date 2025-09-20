package me.snoty.backend.utils.bson

import org.bson.Document

fun Document.getByPath(key: String): Any? {
    val parts = key.split(UNESCAPED_DOT_REGEX)
    if (parts.isEmpty()) throw IllegalArgumentException("Key must not be empty")

    var current: Any? = this

    for (i in parts.indices) {
        val part = parts[i].replace(ESCAPE_REGEX, "$1")
        val isLast = i == parts.lastIndex
        current = when {
            parts[i].matches(ARRAY_INDEX_REGEX) -> current.handleListGet(part, isLast)
            else -> current.handleRegularKeyGet(part, isLast)
        }
        if (current == null) return null
    }
    return current
}

private fun Any?.handleRegularKeyGet(part: String, isLast: Boolean): Any? {
    if (this !is Document) return null
    return if (isLast) this[part] else this[part]
}

private fun Any?.handleListGet(part: String, isLast: Boolean): Any? {
    val key = part.substringBeforeLast("[")
    val index = part.substringAfterLast("[").substringBefore("]").toInt()

    val list = when (this) {
        is Document -> this[key] as? List<*>
        is List<*> -> this
        else -> null
    } ?: return null

    if (index >= list.size) return null
    return if (isLast) list[index] else list[index]
}
