package me.snoty.backend

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.snoty.backend.server.resources.AboutResource
import me.snoty.backend.server.routing.register
import me.snoty.backend.test.TestBuildInfo
import me.snoty.backend.test.ktorApplicationTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.InstanceOfAssertFactories
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuildInfoTest {
	@Test
	fun testBuildInfoEndpoint() = ktorApplicationTest(configure = { AboutResource(TestBuildInfo).register(this) }) {
		client.get("/info").apply {
			assertEquals(HttpStatusCode.OK, status)
			Assertions.assertThat(bodyAsText())
				.isNotEmpty()
			val body = JSONObject(bodyAsText()).toMap()

			Assertions.assertThat(body)
				.containsKeys("hostname", "serverTime", "buildInfo")
				.extractingByKey("buildInfo", InstanceOfAssertFactories.map(String::class.java, String::class.java))
				.containsKeys("gitBranch", "gitCommit", "buildDate", "version", "application")
		}
	}
}
