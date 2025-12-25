package me.snoty.backend.wiring.credential

import com.mongodb.client.model.Filters
import me.snoty.backend.authentication.Role
import me.snoty.backend.utils.hasAnyRole
import me.snoty.backend.wiring.credential.dto.CredentialScope
import org.bson.conversions.Bson

object CredentialFilters {
	fun credentialVisible(userId: String, userRoles: List<Role>): Bson = Filters.or(
		Filters.eq(MongoCredential::scope.name, CredentialScope.GLOBAL),
		Filters.eq(MongoCredential::ownerId.name, userId),
		Filters.`in`(MongoCredential::roleRequired.name, userRoles.map { it.name }),
	)
}

fun MongoCredential.canReadAndWrite(userId: String, userRoles: List<Role>): Boolean = when (scope) {
	CredentialScope.GLOBAL, CredentialScope.ROLE -> userRoles.hasAnyRole(Role.ADMIN, Role.MANAGE_CREDENTIALS)
	CredentialScope.USER -> ownerId == userId
}
