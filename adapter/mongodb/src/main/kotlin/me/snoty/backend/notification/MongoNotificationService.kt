package me.snoty.backend.notification

import com.mongodb.client.model.*
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.snoty.backend.notifications.Notification
import me.snoty.backend.notifications.NotificationAttributes
import me.snoty.backend.notifications.NotificationService
import org.bson.Document
import org.bson.types.ObjectId
import org.koin.core.annotation.Single
import kotlin.time.Clock

@Single
class MongoNotificationService(
	db: MongoDatabase,
) : NotificationService {
	private val collection = db.getCollection<MongoNotification>("notifications")

	init {
		@OptIn(DelicateCoroutinesApi::class)
		GlobalScope.launch(Dispatchers.IO) {
			collection.createIndexes(
				listOf(
					IndexModel(
						Indexes.descending(MongoNotification::userId.name, MongoNotification::attributes.name, MongoNotification::resolvedAt.name),
						IndexOptions().unique(true)
					),
				)
			).collect()
		}
	}

	private fun buildUnresolvedFilter(userId: String, attributes: NotificationAttributes) = Filters.and(
		Filters.eq(MongoNotification::userId.name, userId),
		Filters.eq(MongoNotification::attributes.name, Document(attributes)),
		Filters.eq(MongoNotification::resolvedAt.name, null),
	)

	override suspend fun send(userId: String, attributes: NotificationAttributes, title: String, description: String?) {
		val filter = buildUnresolvedFilter(userId, attributes)
		val update = Updates.combine(
			Updates.setOnInsert(MongoNotification::_id.name, ObjectId()),
			Updates.setOnInsert(MongoNotification::userId.name, userId),
			Updates.setOnInsert(MongoNotification::attributes.name, Document(attributes)),
			Updates.set(MongoNotification::lastSeenAt.name, Clock.System.now()),
			Updates.set(MongoNotification::title.name, title),
			Updates.set(MongoNotification::description.name, description),
			Updates.inc(MongoNotification::count.name, 1)
		)
		collection.updateOne(filter, update, UpdateOptions().upsert(true))
	}

	override suspend fun resolve(userId: String, attributes: NotificationAttributes) {
		val filter = buildUnresolvedFilter(userId, attributes)
		val update = Updates.set(MongoNotification::resolvedAt.name, Clock.System.now())
		collection.updateOne(filter, update)
	}

	override fun findByUser(userId: String): Flow<Notification> =
		collection.find(Filters.eq(MongoNotification::userId.name, userId))
			.sort(Sorts.descending(MongoNotification::lastSeenAt.name))
			.map(MongoNotification::toNotification)

	override suspend fun unresolvedByUser(userId: String): Long =
		collection.countDocuments(
			Filters.and(
				Filters.eq(MongoNotification::userId.name, userId),
				Filters.eq(MongoNotification::resolvedAt.name, null)
			)
		)

	override suspend fun delete(userId: String, id: String): Boolean {
		val result = collection.deleteOne(
			Filters.and(
				Filters.eq(MongoNotification::userId.name, userId),
				Filters.eq(MongoNotification::_id.name, ObjectId(id))
			)
		)
		return result.deletedCount > 0
	}
}
