package me.snoty.backend.notification

import me.snoty.backend.database.sql.PostgresTest
import me.snoty.backend.notifications.NotificationServiceSpec
import me.snoty.backend.notifications.NotificationTable
import me.snoty.backend.notifications.SqlNotificationService
import me.snoty.integration.common.snotyJson
import org.jetbrains.exposed.sql.SchemaUtils

class SqlNotificationServiceTest : NotificationServiceSpec() {
	private val notificationTable = NotificationTable(snotyJson {})
	val db = PostgresTest.getPostgresDatabase {
		SchemaUtils.create(notificationTable)
	}

	override val notificationService = SqlNotificationService(
		db = db,
		table = notificationTable,
	)
}
