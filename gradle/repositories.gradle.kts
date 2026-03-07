dependencyResolutionManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven("https://redirector.kotlinlang.org/maven/ktor-eap")
		mavenLocal()
	}
}

pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://redirector.kotlinlang.org/maven/ktor-eap")
		mavenLocal()
	}
}
