import io.github.simulatan.gradle.plugin.buildinfo.configuration.BuildInfoExtension
import io.github.simulatan.gradle.plugin.buildinfo.configuration.PropertiesOutputLocation

plugins {
	id("io.github.simulatan.gradle-buildinfo-plugin")
}

buildInfo {
	val outputLocation = PropertiesOutputLocation { project ->
		listOf(project.layout.buildDirectory.get().file("info/buildinfo.properties").asFile)
	}
	propertiesOutputs = listOf(outputLocation)
	this.gitInfoMode = BuildInfoExtension.MODE_ERROR
	// ISO date
	committerDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
	buildDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
	extraAttribute("Version", version)
	extraAttribute("Application", rootProject.name)
}

tasks.buildInfo {
	// prevent circular dependency
	dependsOn.clear()
}

project.tasks.named<ProcessResources>("processResources") {
	dependsOn(tasks.buildInfo)
	from("build/info")
}
