package me.snoty.backend.wiring.credential

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import me.snoty.backend.authentication.AuthenticationProvider
import me.snoty.backend.authentication.Role
import me.snoty.backend.test.TestIds
import me.snoty.backend.utils.NotFoundException
import me.snoty.backend.wiring.credential.dto.CredentialScope
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

abstract class CredentialServiceSpec(private val makeId: () -> String) {
	companion object {
		private val USER_ID = TestIds.USER_ID_1.toString()
		private val USER_ID_2 = TestIds.USER_ID_2.toString()
		private val USER_ID_CONTROL = TestIds.USER_ID_CONTROL.toString()
		private val ROLE = Role("user")
	}

	protected val credentialRegistry: TestCredentialRegistry = TestCredentialRegistry()

	protected val authenticationProvider: AuthenticationProvider = mockk()

	init {
		coEvery {
			authenticationProvider.getRolesById(USER_ID)
		} returns listOf(ROLE, Role.MANAGE_CREDENTIALS)

		coEvery {
			authenticationProvider.getRolesById(USER_ID_2)
		} returns listOf(ROLE)

		coEvery {
			authenticationProvider.getRolesById(USER_ID_CONTROL)
		} returns emptyList()
	}

	protected abstract val service: CredentialService

	@Test
	fun testCreateAndGet(): Unit = runBlocking {
		val data = TestCredential(password = "testCreateAndGet")

		val credential = service.create(
			userId = USER_ID,
			scope = CredentialScope.USER,
			role = null,
			name = "My Credential",
			credentialType = "test",
			data = data
		)

		val fetched = service.get(USER_ID, credential.id)
		assertNotNull(fetched)
		assertEquals(credential.data, fetched.data)
	}

	@Test
	fun testGet_AccessControl_User() = runBlocking {
		val data = TestCredential(password = "testGet_AccessControl_User")

		val credential = service.create(
			userId = USER_ID,
			scope = CredentialScope.USER,
			role = null,
			name = "My Credential",
			credentialType = "test",
			data = data
		)

		val fetched = service.get(USER_ID, credential.id)
		assertEquals(credential.id, fetched!!.id)

		val fetched2 = service.get(USER_ID_2, credential.id)
		assertNull(fetched2)

		val fetchedNoAccess = service.get(USER_ID_CONTROL, credential.id)
		assertNull(fetchedNoAccess)

		val credential2 = service.create(
			userId = USER_ID_2,
			scope = CredentialScope.USER,
			role = null,
			name = "My Credential 2",
			credentialType = "test",
			data = data
		)

		val fetchedNoAccess2 = service.get(USER_ID, credential2.id)
		assertNull(fetchedNoAccess2)
	}

	@Test
	fun testGet_AccessControl_Role() = runBlocking {
		val data = TestCredential(password = "testGet_AccessControl_Role")

		val credential = service.create(
			userId = USER_ID, // needs an owner
			scope = CredentialScope.ROLE,
			role = ROLE,
			name = "My Credential",
			credentialType = "test",
			data = data
		)

		val fetched = service.get(USER_ID, credential.id)
		assertEquals(credential.id, fetched!!.id)

		val fetchedWithAccess = service.get(USER_ID_2, credential.id)
		assertEquals(credential.id, fetchedWithAccess!!.id)
		assertNull(fetchedWithAccess.data)

		val fetchedNoAccess = service.get(USER_ID_CONTROL, credential.id)
		assertNull(fetchedNoAccess)
	}

	@Test
	fun testGet_AccessControl_Global() = runBlocking {
		val data = TestCredential(password = "testGet_AccessControl_Global")

		val credential = service.create(
			userId = USER_ID, // needs an owner
			scope = CredentialScope.GLOBAL,
			role = null,
			name = "My Credential",
			credentialType = "test",
			data = data
		)

		val fetched = service.get(USER_ID, credential.id)
		assertEquals(credential.id, fetched!!.id)

		val fetched2 = service.get(USER_ID_2, credential.id)
		assertEquals(credential.id, fetched2!!.id)

		val fetchedControl = service.get(USER_ID_CONTROL, credential.id)
		assertEquals(credential.id, fetchedControl!!.id)
	}

	@Test
	fun testListDefinitionsWithStatistics(): Unit = runBlocking {
		val type = credentialRegistry.registerTestCredential("testListDefinitionsWithStatistics")

		val initial = service.listDefinitionsWithStatistics(USER_ID)
		val initialDefinition = initial.single { it.type == type }
		assertEquals(type, initialDefinition.type)
		assertEquals(TestCredential.DEFINITION.displayName, initialDefinition.displayName)
		assertEquals(0, initialDefinition.count)

		service.create(
			userId = USER_ID,
			scope = CredentialScope.USER,
			role = null,
			name = "My Credential",
			credentialType = type,
			data = TestCredential(password = "testListDefinitionsWithStatistics")
		)

		val afterCreate = service.listDefinitionsWithStatistics(USER_ID)
		val afterCreateDefinition = afterCreate.single { it.type == type }
		assertEquals(type, afterCreateDefinition.type)
		assertEquals(TestCredential.DEFINITION.displayName, afterCreateDefinition.displayName)
		assertEquals(1, afterCreateDefinition.count)
	}

	@Test
	fun testEnumerateCredentials(): Unit = runBlocking {
		val type = credentialRegistry.registerTestCredential("testEnumerateCredentials")

		val created1 = service.create(
			userId = USER_ID,
			scope = CredentialScope.USER,
			role = null,
			name = "Credential 1",
			credentialType = type,
			data = TestCredential(password = "testEnumerateCredentials")
		)

		val created2 = service.create(
			userId = USER_ID,
			scope = CredentialScope.ROLE,
			role = ROLE,
			name = "Credential 2",
			credentialType = type,
			data = TestCredential(password = "testEnumerateCredentials2")
		)

		val created3 = service.create(
			userId = USER_ID_2,
			scope = CredentialScope.USER,
			role = null,
			name = "Credential 3",
			credentialType = type,
			data = TestCredential(password = "testEnumerateCredentials3")
		)

		val credentials = service.enumerateCredentials(USER_ID, type).toList()
		assertEquals(2, credentials.size)
		assertEquals(setOf(created1.id, created2.id), credentials.map { it.id }.toSet())

		val credentials2 = service.enumerateCredentials(USER_ID_2, type).toList()
		assertEquals(2, credentials2.size)
		assertEquals(setOf(created2.id, created3.id), credentials2.map { it.id }.toSet())

		val credentialsControl = service.enumerateCredentials(USER_ID_CONTROL, type).toList()
		assertEquals(0, credentialsControl.size)
	}

	@Test
	fun testListCredentials() = runBlocking {
		val type = credentialRegistry.registerTestCredential("testListCredentials")

		val created = service.create(
			userId = USER_ID,
			scope = CredentialScope.USER,
			role = null,
			name = "My Credential",
			credentialType = type,
			data = TestCredential(password = "testListCredentials")
		)

		val createdUser2 = service.create(
			userId = USER_ID_2,
			scope = CredentialScope.USER,
			role = null,
			name = "User2 Credential",
			credentialType = type,
			data = TestCredential(password = "testListCredentials2")
		)

		val createdRoleAccessible = service.create(
			userId = USER_ID,
			scope = CredentialScope.ROLE,
			role = ROLE,
			name = "My Role Credential",
			credentialType = type,
			data = TestCredential(password = "testListCredentials3")
		)

		val createdGlobal = service.create(
			userId = USER_ID,
			scope = CredentialScope.GLOBAL,
			role = null,
			name = "My Global Credential",
			credentialType = type,
			data = TestCredential(password = "testListCredentials4")
		)

		val credentials = service.listCredentials(USER_ID, type).toList()
		assertEquals(3, credentials.size)

		val fetched = credentials.find { it.id == created.id }
		assertNotNull(fetched)
		assertNotNull(fetched.data)

		val fetchedRoleAccessible = credentials.find { it.id == createdRoleAccessible.id }
		assertNotNull(fetchedRoleAccessible)
		assertEquals(ROLE, fetchedRoleAccessible.requiredRole)
		assertNotNull(fetchedRoleAccessible.data)

		val fetchedGlobal = credentials.find { it.id == createdGlobal.id }
		assertNotNull(fetchedGlobal)
		assertNotNull(fetchedGlobal.data)
		assertNull(fetchedGlobal.requiredRole)

		val credentialsUser2 = service.listCredentials(USER_ID_2, type).toList()
		assertEquals(3, credentialsUser2.size)

		val fetchedUser2 = credentialsUser2.find { it.id == createdUser2.id }
		assertNotNull(fetchedUser2)
		assertNotNull(fetchedUser2.data)

		val fetchedRoleAccessibleUser2 = credentialsUser2.find { it.id == createdRoleAccessible.id }
		assertNotNull(fetchedRoleAccessibleUser2)
		assertEquals(ROLE, fetchedRoleAccessibleUser2.requiredRole)
		assertNull(fetchedRoleAccessibleUser2.data)

		val fetchedGlobalUser2 = credentialsUser2.find { it.id == createdGlobal.id }
		assertNotNull(fetchedGlobalUser2)
		assertNull(fetchedGlobalUser2.data)
		assertNull(fetchedGlobalUser2.requiredRole)

		val credentialsControl = service.listCredentials(USER_ID_CONTROL, type).toList()
		assertEquals(1, credentialsControl.size)
		val fetchedGlobalControl = credentialsControl.find { it.id == createdGlobal.id }
		assertNotNull(fetchedGlobalControl)
		assertNull(fetchedGlobalControl.data)
		assertNull(fetchedGlobalControl.requiredRole)
	}

	@Test
	fun testResolve() = runBlocking {
		val data = TestCredential(password = "testResolve")

		val created = service.create(
			userId = USER_ID,
			scope = CredentialScope.ROLE,
			role = ROLE,
			name = "My Credential",
			credentialType = TestCredential.TYPE,
			data = data
		)

		val resolved = service.resolve(
			userId = USER_ID,
			credentialId = created.id
		)
		assertNotNull(resolved)
		assertEquals(created.id, resolved.id)
		assertEquals(data, resolved.data)

		val resolvedNoAccess = service.resolve(
			userId = USER_ID_2,
			credentialId = created.id
		)
		assertNotNull(resolvedNoAccess)
		assertEquals(created.id, resolvedNoAccess.id)
		assertEquals(data, resolvedNoAccess.data)

		@Serializable
		data class TestCredential2(
			val apiKey: String,
		) : Credential()
		val type = "test2"
		credentialRegistry.register(
			type, TestCredential.DEFINITION.copy(
				type = type,
				clazz = TestCredential2::class.java,
		))

		val created2 = service.create(
			userId = USER_ID,
			scope = CredentialScope.USER,
			role = null,
			name = "My Credential 2",
			credentialType = type,
			data = TestCredential2(apiKey = "testResolve2")
		)

		val resolved2 = service.resolve(
			userId = USER_ID,
			credentialId = created2.id,
			type = TestCredential2::class
		)
		assertNotNull(resolved2)
		assertEquals(created2.id, resolved2.id)
		assertEquals(created2.data, resolved2.data)

		val unresolved = service.resolve(userId = USER_ID, credentialId = makeId())
		assertNull(unresolved)
	}

	@Test
	fun testUpdate(): Unit = runBlocking {
		val data1 = TestCredential(password = "testUpdate")
		val created = service.create(
			userId = USER_ID,
			scope = CredentialScope.USER,
			role = null,
			name = "My Credential",
			credentialType = "test",
			data = data1
		)

		val resolved = service.resolve(USER_ID, created.id)
		assertNotNull(resolved)
		assertEquals(data1, resolved.data)

		val data2 = TestCredential(password = "testUpdate2")
		val updated = service.update(
			userId = USER_ID,
			credential = resolved,
			name = "Updated Credential",
			data = data2
		)
		assertEquals("Updated Credential", updated.name)
		assertEquals(data2, updated.data)
		val resolvedUpdated = service.resolve(USER_ID, created.id)
		assertNotNull(resolvedUpdated)
		assertEquals(data2, resolvedUpdated.data)

		val fetched = service.get(USER_ID, created.id)
		assertNotNull(fetched)
		assertEquals("Updated Credential", fetched.name)
		assertEquals(data2, fetched.data)

		val data3 = TestCredential(password = "testUpdate3")

		assertThrows<NotFoundException> {
			runBlocking { // needed to ensure we can catch the thrown exception
				service.update(USER_ID_2, resolved, "Should Not Work", data3)
			}
		}
	}

	@Test
	fun testDelete(): Unit = runBlocking {
		val created = service.create(
			userId = USER_ID,
			scope = CredentialScope.USER,
			role = null,
			name = "My Credential",
			credentialType = "test",
			data = TestCredential(password = "testDelete")
		)

		val credential = service.resolve(
			userId = USER_ID,
			credentialId = created.id
		)
		assertNotNull(credential)

		val deleted = service.delete(credential)
		assertEquals(true, deleted)

		val fetched = service.get(USER_ID, created.id)
		assertNull(fetched)
	}
}
