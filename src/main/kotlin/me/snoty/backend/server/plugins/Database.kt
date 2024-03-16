package me.snoty.backend.server.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

fun Application.configureDatabases(dataSource: DataSource) {
	Database.connect(dataSource)
}
