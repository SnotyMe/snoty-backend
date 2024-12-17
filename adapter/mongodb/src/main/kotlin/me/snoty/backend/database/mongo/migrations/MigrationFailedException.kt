package me.snoty.backend.database.mongo.migrations

class MigrationFailedException(message: String, cause: Throwable) : RuntimeException(message, cause)
