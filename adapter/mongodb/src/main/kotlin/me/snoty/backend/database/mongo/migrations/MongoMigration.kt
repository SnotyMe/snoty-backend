package me.snoty.backend.database.mongo.migrations

import com.mongodb.kotlin.client.coroutine.ClientSession
import com.mongodb.kotlin.client.coroutine.MongoDatabase

abstract class MongoMigration {
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

	val description: String? = null

	/**
	 * You MUST use the provided session to run your migration. Otherwise, changes will not be tracked correctly and the DB will be left in an inconsistent state when rolling back.
	 */
	abstract fun runMigration(session: ClientSession, database: MongoDatabase)
}
