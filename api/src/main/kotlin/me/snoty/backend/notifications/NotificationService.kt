package me.snoty.backend.notifications

import kotlinx.coroutines.flow.Flow
import me.snoty.core.UserId

interface NotificationService {
	suspend fun send(userId: UserId, attributes: NotificationAttributes, title: String, description: String? = null)

	suspend fun resolve(userId: UserId, attributes: NotificationAttributes)

	fun findByUser(userId: UserId): Flow<Notification>
	suspend fun unresolvedByUser(userId: UserId): Long

	/**
	 * @return true if the notification was deleted, false if it didn't exist
	 */
	suspend fun delete(userId: UserId, id: String): Boolean
}
