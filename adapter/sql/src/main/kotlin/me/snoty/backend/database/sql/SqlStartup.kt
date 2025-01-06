package me.snoty.backend.database.sql

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.exposedLogger
import org.koin.core.annotation.Single

@Single
fun provideDatabase(dataSource: HikariDataSource) = Database.connect(dataSource)
