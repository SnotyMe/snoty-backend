package me.snoty.backend.wiring.credential

import me.snoty.backend.authentication.Role
import me.snoty.backend.utils.bson.decode
import me.snoty.backend.wiring.credential.dto.CredentialDto
import me.snoty.backend.wiring.credential.dto.CredentialScope
import me.snoty.backend.wiring.credential.dto.PotentiallyAccessibleCredentialDto
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class MongoCredential(
	@BsonId
	val _id: ObjectId = ObjectId(),
	val scope: CredentialScope,
	val ownerId: String?,
	val roleRequired: String?,
	val type: String,
	val name: String,
	val data: Document,
) {
	fun toDto(codecRegistry: CodecRegistry, definition: CredentialDefinition) = CredentialDto(
		id = _id.toHexString(),
		scope = scope,
		name = name,
		data = codecRegistry.decode(definition.clazz.kotlin, data)
	)

	fun toPotentiallyAccessibleDto(
		codecRegistry: CodecRegistry,
		definition: CredentialDefinition,
		accessible: Boolean
	) = PotentiallyAccessibleCredentialDto(
		id = _id.toHexString(),
		scope = scope,
		name = name,
		requiredRole = roleRequired?.let { Role(it) },
		data = if (accessible) {
			codecRegistry.decode(definition.clazz.kotlin, data)
		} else {
			null
		}
	)
}
