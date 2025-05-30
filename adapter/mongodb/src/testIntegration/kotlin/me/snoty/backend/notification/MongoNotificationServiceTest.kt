package me.snoty.backend.notification

import me.snoty.backend.database.mongo.MongoTest
import me.snoty.backend.notifications.NotificationServiceSpec

class MongoNotificationServiceTest : NotificationServiceSpec() {
	private val mongoDB = MongoTest.getMongoDatabase {}

	override val notificationService = MongoNotificationService(
		db = mongoDB,
	)
}
