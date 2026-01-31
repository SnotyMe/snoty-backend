package me.snoty.backend.notification

import me.snoty.backend.database.sql.PostgresTest
import me.snoty.backend.notifications.NotificationServiceSpec
import me.snoty.backend.notifications.NotificationTable
import me.snoty.backend.notifications.SqlNotificationService
import me.snoty.integration.common.snotyJson
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.update
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Clock

class SqlNotificationServiceTest : NotificationServiceSpec() {
	private val notificationTable = NotificationTable(snotyJson {})
	val db = PostgresTest.getPostgresDatabase {
		SchemaUtils.create(notificationTable)
	}

	override val notificationService = SqlNotificationService(
		db = db,
		table = notificationTable,
	)

	@Test
	fun inconsistentResolvedState() = test {
		val attributes = attributes("key" to "value")
		notificationService.send(attributes)

		assertThrows<IllegalStateException> {
			notificationTable.update(where = {
				notificationTable.userId eq userId and (notificationTable.attributes eq attributes)
			}) {
				it[notificationTable.open] = true
				it[notificationTable.resolvedAt] = null
			}
		}

		assertThrows<IllegalStateException> {
			notificationTable.update(where = {
				notificationTable.userId eq userId and (notificationTable.attributes eq attributes)
			}) {
				it[notificationTable.open] = false
				it[notificationTable.resolvedAt] = Clock.System.now()
			}
		}
	}

	@Test
	fun openFalseIsIllegal() = test {
		val attributes = attributes("key" to "value")
		notificationService.send(attributes)

		assertThrows<IllegalStateException> {
			notificationTable.update(where = {
				notificationTable.userId eq userId and (notificationTable.attributes eq attributes)
			}) {
				it[notificationTable.open] = false
			}
		}
	}
}
