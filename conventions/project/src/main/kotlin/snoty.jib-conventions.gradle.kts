import org.eclipse.jgit.api.Git

plugins {
	id("com.google.cloud.tools.jib")
}

jib {
	from {
		image = "eclipse-temurin:21-jre-alpine"
	}
	to {
		val allTags = project.properties["me.snoty.docker.tags"]?.toString()?.trim()?.split(" ")?.toSet()
			?: setOf(version.toString())
		image = "ghcr.io/snotyme/snoty-backend:${allTags.first()}"
		// workaround for the TERRIBLE design decisions of the JIB developers to
		// still generate the `latest` tag even when tags are specified...
		if (allTags.size > 1) {
			tags = allTags.drop(1).toSet()
		}
	}
	container {
		jvmFlags = listOf("-Dio.ktor.development=false")
		extraClasspath = extraClasspath + "/integrations/*"
		creationTime = "USE_CURRENT_TIMESTAMP"
		appRoot = "/app"
		workingDirectory = "/app"
		ports = listOf("8080")
		val (ghaRunId, ghaRunNumber) =
			project.properties["me.snoty.github.run"]?.toString()?.split(":") ?: listOf(null, null)
		labels = mapOf(
			"org.opencontainers.image.title" to "snoty-backend",
			"org.opencontainers.image.description" to "Backend for the snoty project",
			"org.opencontainers.image.url" to "https://github.com/SnotyMe/snoty-backend/pkgs/container/snoty-backend",
			*Git.open(project.rootDir).use { git ->
				val headRef = git.repository.resolve("HEAD").name
				arrayOf(
					"org.opencontainers.image.revision" to headRef,
					// source to https version of the git repository
					"org.opencontainers.image.source" to git.repository.config.getString("remote", "origin", "url")
						.replace(":", "/")
						.replace("git@", "https://")
						.replace(".git", "")
						+ "/tree/$headRef"
				)
			},
			*if (ghaRunId != null && ghaRunNumber != null) arrayOf(
				"com.github.actions.run.id" to ghaRunId,
				"com.github.actions.run.number" to ghaRunNumber
			) else arrayOf()
		)
	}
}
