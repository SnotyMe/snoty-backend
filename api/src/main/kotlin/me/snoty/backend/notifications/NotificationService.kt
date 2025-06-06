package me.snoty.backend.notifications

import kotlinx.coroutines.flow.Flow

interface NotificationService {
	suspend fun send(userId: String, attributes: NotificationAttributes, title: String, description: String? = null)

	suspend fun resolve(userId: String, attributes: NotificationAttributes)

	fun findByUser(userId: String): Flow<Notification>
	suspend fun unresolvedByUser(userId: String): Long

	/**
	 * @return true if the notification was deleted, false if it didn't exist
	 */
	suspend fun delete(userId: String, id: String): Boolean
}
