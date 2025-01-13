package me.snoty.backend.database.sql

import org.jetbrains.exposed.sql.Database
import org.koin.core.annotation.Single
import javax.sql.DataSource

@Single
fun provideDatabase(dataSource: DataSource) = Database.connect(dataSource)
