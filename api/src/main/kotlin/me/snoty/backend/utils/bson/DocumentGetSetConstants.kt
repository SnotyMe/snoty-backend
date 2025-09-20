package me.snoty.backend.utils.bson

val UNESCAPED_DOT_REGEX = """(?<!\\)\.""".toRegex()
val ESCAPE_REGEX = """\\(.)""".toRegex()
val ARRAY_INDEX_REGEX = """^.*(?:[^\\]|\\\\)\[\d+]$""".toRegex()
