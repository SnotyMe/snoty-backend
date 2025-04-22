package me.snoty.backend.notifications

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.snoty.backend.test.randomString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

abstract class NotificationServiceSpec {
	abstract val notificationService: NotificationService

	data class TestScope(
		val testName: String,
		val userId: String = randomString(),
	)

	protected fun test(block: suspend TestScope.() -> Unit) {
		val testName = block.javaClass.typeName.substringAfter("$").substringBefore("$")
		runBlocking {
			block(TestScope(testName = testName))
		}
	}

	protected fun TestScope.attributes(vararg pairs: Pair<String, String>): NotificationAttributes
		= mapOf(*pairs, "testName" to testName)

	@Test
	fun simpleSend() = test {
		val attributes = attributes("key" to "value")
		notificationService.send(userId, attributes)

		val notifications = notificationService.findByUser(userId).toList()
		assertEquals(Notification(
			userId = userId,
			attributes = attributes,
			resolvedAt = null,
			lastSeenAt = notifications.first().lastSeenAt,
			count = 1,
		), notifications.single())
	}

	@Test
	fun simpleSendMultipleTimes() = test {
		val attributes = attributes("key" to "value")

		val expectedCount = 5
		repeat(expectedCount) {
			notificationService.send(userId, attributes)
		}

		val notifications = notificationService.findByUser(userId).toList()
		assertEquals(Notification(
			userId = userId,
			attributes = attributes,
			resolvedAt = null,
			lastSeenAt = notifications.first().lastSeenAt,
			count = expectedCount,
		), notifications.single())
	}

	@Test
	fun simpleResolve() = test {
		val attributes = attributes("key" to "value")

		notificationService.send(userId, attributes)
		notificationService.send(userId, attributes)
		assertNull(notificationService.findByUser(userId).single().resolvedAt)

		notificationService.resolve(userId, attributes)
		assertNotNull(notificationService.findByUser(userId).single().resolvedAt)

		val notification = notificationService.findByUser(userId).single()
		assertEquals(notification.userId, userId)
		assertEquals(notification.attributes, attributes)
		assertEquals(notification.count, 2)
		assertNotNull(notification.lastSeenAt)
		assertNotNull(notification.resolvedAt)
		assertTrue(notification.lastSeenAt <= notification.resolvedAt)
	}

	@Test
	fun interference() = test {
		val userId = "userId"
		val attributes1 = attributes("key" to "value")
		val attributes2 = attributes("key" to "value2")

		notificationService.send(userId, attributes1)
		notificationService.send(userId, attributes1)
		notificationService.send(userId, attributes2)
		assertEquals(2, notificationService.findByUser(userId).toList().size)

		notificationService.resolve(userId, attributes1)

		val notifications = notificationService.findByUser(userId).toList()
		assertEquals(2, notifications.size)

		val notification1 = notifications.first { it.attributes == attributes1 }
		val notification2 = notifications.first { it.attributes == attributes2 }

		assertEquals(userId, notification1.userId)
		assertEquals(attributes1, notification1.attributes)
		assertNotNull(notification1.resolvedAt)
		assertNotNull(notification1.lastSeenAt)
		assertTrue(notification1.lastSeenAt <= notification1.resolvedAt)
		assertEquals(2, notification1.count)

		assertEquals(userId, notification2.userId)
		assertEquals(attributes2, notification2.attributes)
		assertNull(notification2.resolvedAt)
		assertNotNull(notification2.lastSeenAt)
		assertEquals(1, notification2.count)
	}

	@Test
	fun resolvedIsRecreated() = test {
		val attributes = attributes("key" to "value")

		notificationService.send(userId, attributes)
		notificationService.resolve(userId, attributes)

		val notification = notificationService.findByUser(userId).toList()
		assertEquals(1, notification.size)

		val notification1 = notification.first { it.attributes == attributes }
		assertEquals(userId, notification1.userId)
		assertEquals(attributes, notification1.attributes)
		assertNotNull(notification1.resolvedAt)
		assertNotNull(notification1.lastSeenAt)

		// already resolved with these attributes, should be recreated, sent twice without resolving => count = 2
		notificationService.send(userId, attributes)
		val notification2WithCount1 = notificationService.findByUser(userId).toList().single { it != notification1 }
		assertEquals(1, notification2WithCount1.count)

		// some artificial delay
		delay(100.milliseconds)

		notificationService.send(userId, attributes)
		val notifications = notificationService.findByUser(userId).toList()
		assertEquals(2, notifications.size)

		val notification2 = notifications.single { it != notification1 }
		assertEquals(userId, notification2.userId)
		assertEquals(attributes, notification2.attributes)
		assertNull(notification2.resolvedAt)
		assertNotNull(notification2.lastSeenAt)
		assertEquals(2, notification2.count)

		assertNotEquals(notification2WithCount1.lastSeenAt, notification2.lastSeenAt)
	}
}
