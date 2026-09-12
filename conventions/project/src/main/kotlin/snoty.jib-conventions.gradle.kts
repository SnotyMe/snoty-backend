import org.eclipse.jgit.api.Git

plugins {
	id("com.google.cloud.tools.jib")
}

jib {
	val fullImageName = providers.gradleProperty("me.snoty.docker.image")
		.getOrElse("ghcr.io/snotyme/snoty-backend")

	from {
		image = "eclipse-temurin:21-jre-alpine"
	}
	to {
		val allTags = providers.gradleProperty("me.snoty.docker.tags").orNull?.trim()?.split(" ")?.toSet()
			?: setOf(version.toString())
		image = "$fullImageName:${allTags.first()}"
		// workaround for the TERRIBLE design decisions of the JIB developers to
		// still generate the `latest` tag even when tags are specified...
		if (allTags.size > 1) {
			tags = allTags.drop(1).toSet()
		}
	}
	container {
		// https://github.com/swagger-api/swagger-codegen-generators/issues/1015
		jvmFlags = listOf("-Dio.ktor.development=false", "-Dlogback.configurationFile=logback.xml")
		extraClasspath = extraClasspath + "/integrations/*"
		creationTime = "USE_CURRENT_TIMESTAMP"
		appRoot = "/app"
		workingDirectory = "/app"
		ports = listOf("8080")

		val (ghaRunId, ghaRunNumber) =
			providers.gradleProperty("me.snoty.github.run").orNull?.split(":") ?: listOf(null, null)

		Git.open(project.rootDir).use { git ->
			val repoUrl = git.repository.config
				.getString("remote", "origin", "url")
				.replace(":", "/")
				.replace("git@", "https://")
				.replace(".git", "")
			val headRef = git.repository.resolve("HEAD").name

			val imageTitle = fullImageName.substringAfterLast("/")
			labels = mapOf(
				"org.opencontainers.image.title" to imageTitle,
				"org.opencontainers.image.description" to "Backend for the snoty project",
				"org.opencontainers.image.url" to "$repoUrl/pkgs/container/$imageTitle",
				"org.opencontainers.image.revision" to headRef,
				"org.opencontainers.image.source" to "$repoUrl/tree/$headRef",
				*if (ghaRunId != null && ghaRunNumber != null) arrayOf(
					"com.github.actions.run.id" to ghaRunId,
					"com.github.actions.run.number" to ghaRunNumber
				) else arrayOf()
			)
		}
	}
}
