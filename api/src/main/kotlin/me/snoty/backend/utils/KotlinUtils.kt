package me.snoty.backend.utils

fun <T, C : Collection<T>> C.orNull() = ifEmpty { null }
