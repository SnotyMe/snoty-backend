plugins {
	`kotlin-dsl`
	`maven-publish`
}

apply(from = "../../version.gradle.kts")

dependencies {
	libs.plugins.kotlin.jvm.get().apply {
		implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$version")
	}
	libs.plugins.kotlin.kover.get().apply {
		implementation("org.jetbrains.kotlinx:kover-gradle-plugin:$version")
	}
	libs.plugins.jib.get().apply {
		implementation("com.google.cloud.tools:jib-gradle-plugin:$version")
	}
	libs.plugins.ksp.get().apply {
		implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$version")
	}
	implementation("org.eclipse.jgit:org.eclipse.jgit:6.10.0.202406032230-r")
	libs.plugins.idea.get().apply {
		implementation("gradle.plugin.org.jetbrains.gradle.plugin.idea-ext:gradle-idea-ext:$version")
	}
	libs.plugins.doctor.get().apply {
		implementation("com.osacky.doctor:doctor-plugin:$version")
	}
	libs.plugins.buildinfo.get().apply {
		implementation("io.github.simulatan:gradle-buildinfo-plugin:$version")
	}

	libs.koin.let { koin ->
		listOf(koin.core, koin.ksp, koin.annotations)
	}.forEach {
		implementation(it)
	}
}

publishing {
	publications
		.configureEach {
			if (this is MavenPublication) {
				artifactId = "conventions"
			}
		}
}
