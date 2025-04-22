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
	) {
		suspend fun NotificationService.send(attributes: NotificationAttributes, userId: String? = null) =
			send(userId ?: this@TestScope.userId, attributes, testName, null)
	}

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
		notificationService.send(attributes)

		val notifications = notificationService.findByUser(userId).single()
		assertEquals(userId, notifications.userId)
		assertEquals(attributes, notifications.attributes)
		assertNull(notifications.resolvedAt)
		assertNotNull(notifications.lastSeenAt)
		assertEquals(1, notifications.count)
	}

	@Test
	fun simpleSendMultipleTimes() = test {
		val attributes = attributes("key" to "value")

		repeat(5) {
			val title = "Iteration $it"
			val description = if (it % 2 == 0) "Even" else null
			notificationService.send(userId = userId, attributes = attributes, title = title, description = description)

			val notification = notificationService.findByUser(userId).single()
			assertEquals(userId, notification.userId)
			assertEquals(attributes, notification.attributes)
			assertNull(notification.resolvedAt)
			assertNotNull(notification.lastSeenAt)
			assertEquals(it + 1, notification.count)
			assertEquals(title, notification.title)
			assertEquals(description, notification.description)
		}
	}

	@Test
	fun simpleResolve() = test {
		val attributes = attributes("key" to "value")

		notificationService.send(attributes)
		notificationService.send(attributes)
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

		notificationService.send(attributes1, userId)
		notificationService.send(attributes1, userId)
		notificationService.send(attributes2, userId)
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

		notificationService.send(attributes)
		notificationService.resolve(userId, attributes)

		val notification = notificationService.findByUser(userId).toList()
		assertEquals(1, notification.size)

		val notification1 = notification.first { it.attributes == attributes }
		assertEquals(userId, notification1.userId)
		assertEquals(attributes, notification1.attributes)
		assertNotNull(notification1.resolvedAt)
		assertNotNull(notification1.lastSeenAt)

		// already resolved with these attributes, should be recreated, sent twice without resolving => count = 2
		notificationService.send(attributes)
		val notification2WithCount1 = notificationService.findByUser(userId).toList().single { it != notification1 }
		assertEquals(1, notification2WithCount1.count)

		// some artificial delay
		delay(100.milliseconds)

		notificationService.send(attributes)
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
