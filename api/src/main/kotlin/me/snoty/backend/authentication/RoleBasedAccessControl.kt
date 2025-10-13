package me.snoty.backend.authentication

fun List<Role>.canManageCredentials() =
	any { it == Role.ADMIN || it == Role.MANAGE_CREDENTIALS }
