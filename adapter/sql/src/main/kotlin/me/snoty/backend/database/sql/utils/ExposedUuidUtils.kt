package me.snoty.backend.database.sql.utils

import org.jetbrains.exposed.v1.core.dao.id.UuidTable

open class UuidTable(name: String = "", columnName: String = "id") :
	UuidTable(name, columnName, UuidVersion.V7)
