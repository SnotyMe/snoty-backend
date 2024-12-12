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

				credentials(PasswordCredentials::class)
				authentication {
					create("basic", BasicAuthentication::class)
				}
			}
		}
	}
}
