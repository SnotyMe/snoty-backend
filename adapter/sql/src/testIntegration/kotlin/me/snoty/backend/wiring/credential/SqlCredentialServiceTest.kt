package me.snoty.backend.wiring.credential

import me.snoty.backend.database.sql.PostgresTest
import me.snoty.integration.common.snotyJson
import kotlin.uuid.Uuid

class SqlCredentialServiceTest : CredentialServiceSpec({ Uuid.random().toString() }) {
	private val db = PostgresTest.getPostgresDatabase {}

	private val credentialTable = CredentialTable()

	override val service: CredentialService = SqlCredentialService(
		db = db,
		json = snotyJson {},
		registry = credentialRegistry,
		credentialTable = credentialTable,
		authenticationProvider = authenticationProvider,
	)
}
