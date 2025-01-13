package me.snoty.backend.scheduling.jobrunr

import org.jobrunr.storage.StorageProvider
import org.jobrunr.storage.sql.postgres.PostgresStorageProvider
import org.koin.core.annotation.Single
import javax.sql.DataSource

const val JOBRUNR_TABLE_PREFIX = "jobrunr$"

@Single
fun provideStorageProvider(dataSource: DataSource): StorageProvider = PostgresStorageProvider(
	/* dataSource = */ dataSource,
	/* tablePrefix = */ JOBRUNR_TABLE_PREFIX
)
