package me.snoty.backend.wiring.credential.dto

enum class CredentialAccess {
	USER,

	/**
	 * System-wide credential, not tied to any specific user. Not readable or editable by regular users.
	 */
	SYSTEM,
}
