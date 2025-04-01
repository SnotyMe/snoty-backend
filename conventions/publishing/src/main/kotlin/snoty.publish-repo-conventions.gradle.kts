plugins {
	kotlin("jvm")
	`maven-publish`
}

publishing {
	repositories {
		listOf("Releases", "Snapshots").map {
			maven {
				name = "snoty$it"
				url = uri("https://maven.snoty.me/${it.lowercase()}")

				val envVarPrefix = "MAVEN_${it.uppercase()}"
				val envUser = System.getenv("${envVarPrefix}_USERNAME")
				val envPassword = System.getenv("${envVarPrefix}_PASSWORD")
				if (envUser != null && envPassword != null) {
					credentials {
						username = envUser
						password = envPassword
					}
				} else {
					credentials(PasswordCredentials::class)
					authentication {
						create("basic", BasicAuthentication::class)
					}
				}
			}
		}
	}
}
