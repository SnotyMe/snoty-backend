package me.snoty.backend.scheduling.jobrunr

import me.snoty.backend.database.sql.PostgresTest

class SqlJobRunrStorageProviderTest : JobRunrStorageProviderSpec() {
	private val dataSource = PostgresTest.getPostgresDataSource {}
	override val storageProvider = SqlJobRunrStorageProvider(dataSource)
}
