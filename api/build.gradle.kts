plugins {
    alias(libs.plugins.kotlin.serialization)
    id("snoty.publish-conventions")
    `version-catalog`
}

dependencies { with(libs) {
    implementation(configuration.hoplite.core)

    api(libraries.kotlinx.serialization)
    api(libraries.kotlinx.datetime)

    api(libraries.jobrunr)

    api(log.kotlinLogging)

    api(ktor.client.core)
    api(ktor.client.apache)
    api(ktor.client.contentNegotiation)

    api(ktor.server.core)
    implementation(ktor.server.auth)
    implementation(ktor.server.auth.jwt)

    api(ktor.serialization.kotlinx.json)
    implementation(monitoring.ktor.opentelemetry)

    implementation(libraries.openfeature)

    api(libraries.bson.kotlinx)
    api(database.mongodb)

    implementation(monitoring.micrometer)
    api(monitoring.opentelemetry.api)
    api(monitoring.opentelemetry.context)

    testImplementation(tests.junit.api)
    testImplementation(tests.mockk)
    testImplementation(kotlin("test"))
}}

tasks.test {
    useJUnitPlatform()
}

catalog {
    versionCatalog {
        versionCatalogs.forEach { catalog ->
            catalog.versionAliases.forEach { alias ->
                version(alias, catalog.findVersion(alias).get().displayName)
            }
            version("snoty", version.toString())

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
