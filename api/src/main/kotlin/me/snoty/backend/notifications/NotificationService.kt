package me.snoty.backend.notifications

import kotlinx.coroutines.flow.Flow

interface NotificationService {
	suspend fun send(userId: String, attributes: NotificationAttributes)

	suspend fun resolve(userId: String, attributes: NotificationAttributes)

	fun findByUser(userId: String): Flow<Notification>
}
