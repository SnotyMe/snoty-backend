package me.snoty.backend.database.sql.migrations

class MigrationFailedException : RuntimeException {
	constructor(message: String, cause: Throwable) : super(message, cause)
	constructor(message: String) : super(message)
}
