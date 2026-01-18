package me.snoty.backend.errors

import me.snoty.backend.utils.BadRequestException

class InvalidIdException(override val cause: Exception) : BadRequestException("Invalid ID format")
