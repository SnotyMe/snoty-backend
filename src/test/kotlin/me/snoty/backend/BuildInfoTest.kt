package me.snoty.backend

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import me.snoty.backend.server.resources.AboutResource
import me.snoty.backend.server.routing.register
import me.snoty.backend.test.TestBuildInfo
import me.snoty.backend.test.ktorApplicationTest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class BuildInfoTest {
	@Test
	fun testBuildInfoEndpoint() = ktorApplicationTest(routes = { AboutResource(TestBuildInfo).register(this) }) {
		client.get("/info").apply {
			assertEquals(HttpStatusCode.OK, status)
			val body = JSONObject(bodyAsText())

			assertNotNull(body.getString("hostname"))
			val serverTimeRaw = body.getString("serverTime")
			assertNotNull(serverTimeRaw)
			assertTrue(Instant.parse(serverTimeRaw) in System.now().minus(10.seconds)..System.now())

			val buildInfo = body.getJSONObject("buildInfo")
			assertEquals(TestBuildInfo.gitBranch, buildInfo.getString("gitBranch"))
			assertEquals(TestBuildInfo.gitCommit, buildInfo.getString("gitCommit"))
			assertEquals(TestBuildInfo.buildDate.toString(), buildInfo.getString("buildDate"))
			assertEquals(TestBuildInfo.version, buildInfo.getString("version"))
			assertEquals(TestBuildInfo.application, buildInfo.getString("application"))
		}
	}
}
