package me.snoty.backend.scheduling.jobrunr

import me.snoty.backend.database.sql.schema
import org.jobrunr.jobs.states.StateName
import org.jobrunr.storage.StorageException
import org.jobrunr.storage.StorageProvider
import org.jobrunr.storage.StorageProviderUtils.Jobs.FIELD_RECURRING_JOB_ID
import org.jobrunr.storage.sql.common.JobTable
import org.jobrunr.storage.sql.postgres.PostgresStorageProvider
import org.koin.core.annotation.Single
import java.sql.SQLException
import javax.sql.DataSource

@Single(binds = [SnotyJobRunrStorageProvider::class, StorageProvider::class])
fun provideStorageProvider(dataSource: DataSource): SnotyJobRunrStorageProvider = SqlJobRunrStorageProvider(dataSource)

class SqlJobRunrStorageProvider(dataSource: DataSource) : PostgresStorageProvider(
	/* dataSource = */ dataSource,
	/* tablePrefix = */ "${dataSource.schema}."
), SnotyJobRunrStorageProvider {
	override fun recurringJobExists(recurringJobId: String, vararg states: StateName): Boolean {
		try {
			dataSource.connection.use { conn ->
				return jobTable(conn).recurringJobExists(recurringJobId, *states)
			}
		} catch (e: SQLException) {
			throw StorageException(e)
		}
	}
}

private fun JobTable.recurringJobExists(recurringJobId: String, vararg states: StateName): Boolean {
	if (states.isEmpty()) {
		return with(FIELD_RECURRING_JOB_ID, recurringJobId)
			.selectExists("from jobrunr_jobs where recurringJobId = :recurringJobId")
	}

	return with(FIELD_RECURRING_JOB_ID, recurringJobId)
		.selectExists(
			"from jobrunr_jobs where state in (" + states.joinToString(separator = ",") { "'${it.name}'" } + ") AND recurringJobId = :recurringJobId"
		)
}
