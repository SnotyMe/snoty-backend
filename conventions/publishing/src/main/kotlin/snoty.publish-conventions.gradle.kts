plugins {
	kotlin("jvm")
	`maven-publish`
}

java {
	withSourcesJar()
}

publishing {
	if (publications.isNotEmpty()) return@publishing

	publications {
		create<MavenPublication>(name.replace("-", "")) {
			artifactId = project.name

			pom {
				scm {
					url = "scm:git:https://github.com/SnotyMe/snoty-backend"
				}
			}

			from(components["java"])
		}
	}
}
