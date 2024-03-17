package me.snoty.backend

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.snoty.backend.test.ktorApplicationTest
import org.assertj.core.api.Assertions
import org.json.JSONObject
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildInfoTest {
	@Test
	fun testBuildInfoEndpoint() = ktorApplicationTest {
		client.get("/info").apply {
			assertEquals(HttpStatusCode.OK, status)
			Assertions.assertThat(bodyAsText())
				.isNotEmpty()
			val body = JSONObject(bodyAsText()).toMap()

			Assertions.assertThat(body)
				.containsKeys("gitBranch", "gitCommit", "buildDate", "version", "application")
		}
	}
}
