package me.snoty.backend.scheduling.jobrunr

import me.snoty.backend.database.sql.PostgresTest

class SqlJobrunrStorageProviderTest : JobrunrStorageProviderSpec() {
	private val dataSource = PostgresTest.getPostgresDataSource {}
	override val storageProvider = SqlJobrunrStorageProvider(dataSource)
}
