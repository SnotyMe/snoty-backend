package me.snoty.backend.database.sql

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.db.DataSourceManager
import com.sksamuel.cohort.db.DatabaseConnectionHealthCheck
import com.sksamuel.cohort.hikari.HikariDataSourceManager
import com.sksamuel.cohort.hikari.HikariPendingThreadsHealthCheck
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import javax.sql.DataSource
import kotlin.time.Duration.Companion.milliseconds

@Single
@Named("hikariDataSourceManager")
fun provideHikariDataSourceManager(dataSource: DataSource): DataSourceManager =
	HikariDataSourceManager(dataSource.unwrapHikariDs())

@Single
@Named("hikariPendingThreadsHealthCheck")
fun provideHikariPendingThreadsHealthCheck(dataSource: DataSource): HealthCheck =
	HikariPendingThreadsHealthCheck(dataSource.unwrapHikariDs(), maxAwaiting = 5)

@Single
@Named("databaseConnectionHealthCheck")
fun provideDatabaseConnectionHealthCheck(dataSource: DataSource): HealthCheck =
	DatabaseConnectionHealthCheck(dataSource, dataSource.unwrapHikariDs().validationTimeout.milliseconds)
