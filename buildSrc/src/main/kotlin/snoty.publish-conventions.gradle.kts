plugins {
	kotlin("jvm")
	`maven-publish`
}

publishing {
	if (publications.isNotEmpty()) return@publishing

	publications {
		create<MavenPublication>(name.replace("-", "")) {
			artifactId = project.name

			from(components["kotlin"])
		}
	}
}
