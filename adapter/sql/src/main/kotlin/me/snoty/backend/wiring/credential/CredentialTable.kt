package me.snoty.backend.wiring.credential

import me.snoty.backend.database.sql.utils.UuidTable
import me.snoty.backend.database.sql.utils.rawJsonb
import me.snoty.backend.wiring.credential.dto.CredentialScope
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.koin.core.annotation.Single

@Single(binds = [Table::class])
class CredentialTable : UuidTable("credential") {
	val scope = enumerationByName<CredentialScope>("scope", 50)

	val ownerId = varchar("owner_id", 255).nullable()
	val roleRequired = varchar("role_required", 255).nullable()

	val type = varchar("type", 255)

	val name = text("name")

	val data = rawJsonb<Credential>("data")

	init {
		check {
			(scope eq CredentialScope.USER and ownerId.isNotNull() and roleRequired.isNull())
				.or(scope eq CredentialScope.ROLE and roleRequired.isNotNull())
				.or(scope eq CredentialScope.GLOBAL and roleRequired.isNull())
		}
	}
}
