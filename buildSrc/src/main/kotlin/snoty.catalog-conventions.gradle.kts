plugins {
	`version-catalog`
	`maven-publish`
}

catalog {
	versionCatalog {
		versionCatalogs.forEach { catalog ->
			catalog.versionAliases.forEach { alias ->
				version(alias, catalog.findVersion(alias).get().displayName)
			}
			version("snoty", rootProject.version.toString())

			// map of version - alias | needed to guess the aliases used
			val versions = catalog.versionAliases.associate { alias ->
				val version = catalog.findVersion(alias)
				version.get().displayName to alias.replace(".", "-")
			}
			catalog.libraryAliases.forEach { alias ->
				val lib = catalog.findLibrary(alias).get().get()
				library(alias, lib.group, lib.name)
					.let {
						val libVersion = lib.version!!
						when (val version = versions[libVersion]) {
							null -> it.version(libVersion)
							else -> it.versionRef(version)
						}
					}
			}
		}
	}
}

publishing {
	publications {
		create<MavenPublication>("versionCatalog") {
			from(components["versionCatalog"])
			artifactId = "versions"
		}
	}
}
