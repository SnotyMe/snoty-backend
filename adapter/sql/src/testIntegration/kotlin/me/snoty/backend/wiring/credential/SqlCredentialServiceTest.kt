package me.snoty.backend.wiring.credential

import me.snoty.backend.database.sql.PostgresTest
import me.snoty.integration.common.snotyJson
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import kotlin.uuid.Uuid

class SqlCredentialServiceTest : CredentialServiceSpec({ Uuid.random().toString() }) {
	private val credentialTable = CredentialTable()

	private val db = PostgresTest.getPostgresDatabase {
		SchemaUtils.create(credentialTable)
	}

	override val service: CredentialService = SqlCredentialService(
		db = db,
		json = snotyJson {},
		registry = credentialRegistry,
		credentialTable = CredentialTable(),
		authenticationProvider = authenticationProvider,
	)
}
