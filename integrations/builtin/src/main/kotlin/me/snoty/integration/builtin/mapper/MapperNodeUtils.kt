package me.snoty.integration.builtin.mapper

import org.bson.Document

fun Document.trimAll(): Document = apply {
	forEach { (key, value) ->
		when (value) {
			is String -> this[key] = value.trimEveryLine()
			is Collection<*> -> this[key] = value.trimAll()
			is Document -> value.trimAll()
		}
	}
}

private fun Collection<*>.trimAll(): List<*> = map {
	when (it) {
		is String -> it.trimEveryLine()
		is Document -> it.trimAll()
		is Collection<*> -> it.trimAll()
		else -> it // Keep other types as is
	}
}

private fun String.trimEveryLine() =
	split("\n").joinToString("\n") { it.trim() }
