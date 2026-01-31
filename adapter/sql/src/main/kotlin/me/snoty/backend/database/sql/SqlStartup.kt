package me.snoty.backend.database.sql

import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.annotation.Single
import javax.sql.DataSource

@Single
fun provideDatabase(dataSource: DataSource) = Database.connect(dataSource)
