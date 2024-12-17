package me.snoty.backend.database.mongo.migrations

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging

abstract class MongoMigration(val appVersion: String) {
	/**
	 * Namespace of the migration. Usually the package name of the migration.
	 * Use this to avoid re-running the migration when you relocate to another package.
	 */
	open val namespace: String = javaClass.packageName
	protected val logger = KotlinLogging.logger {}

	/*
	 * NEVER CHANGE THIS NAME
	 * DO NOT CHANGE THIS NAME
	 * IF YOU DO CHANGE THIS NAME I WILL BLACKLIST YOUR ENTIRE EXISTENCE FROM THE INTERNET
	 * THIS NAME IS USED TO IDENTIFY THE MIGRATION IN THE DATABASE
	 * IF YOU CHANGE THIS NAME, THE MIGRATION WILL RUN AGAIN
	 * AND YOU WILL BE RESPONSIBLE FOR THE CONSEQUENCES
	 * YOU HAVE BEEN WARNED
	 * DO NOT CHANGE THIS NAME
	 * NEVER CHANGE THIS NAME
	 * DO NOT CHANGE THIS NAME
	 */
	abstract val name: String
	open val description: String? = null

	abstract suspend fun execute(database: MongoDatabase)
	abstract suspend fun rollback(database: MongoDatabase)
}
