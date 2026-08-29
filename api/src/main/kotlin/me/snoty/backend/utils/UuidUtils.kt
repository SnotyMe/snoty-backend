package me.snoty.backend.utils

import me.snoty.backend.errors.InvalidIdException
import kotlin.uuid.Uuid

fun String.toUuid() = try {
	Uuid.parse(this)
} catch (e: IllegalArgumentException) {
	throw InvalidIdException(e)
}
