package me.snoty.backend.utils

import java.util.*

@Suppress("UNCHECKED_CAST") // Kotlin type system limitation with generics and Java Optional
fun <T> optionalOf(value: T): Optional<T> = Optional.ofNullable(value) as Optional<T>
