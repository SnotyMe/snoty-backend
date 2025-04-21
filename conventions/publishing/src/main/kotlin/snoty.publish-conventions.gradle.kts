plugins {
	kotlin("jvm")
	`maven-publish`
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

			from(components["kotlin"])
		}
	}
}
