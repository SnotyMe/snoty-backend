package me.snoty.backend.wiring.credential

import me.snoty.backend.database.sql.utils.UuidTable
import me.snoty.backend.database.sql.utils.rawJsonb
import org.jetbrains.exposed.sql.Table
import org.koin.core.annotation.Single

@Single(binds = [Table::class])
class CredentialTable : UuidTable("credential") {
	val userId = varchar("user_id", 255).nullable()
	val roleRequired = varchar("role_required", 255).nullable()

	val type = varchar("type", 255)

	val name = text("name")

	val data = rawJsonb<Credential>("data")
}
