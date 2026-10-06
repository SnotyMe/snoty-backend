dependencyResolutionManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven("https://redirector.kotlinlang.org/maven/ktor-eap")

		// https://github.com/node-gradle/gradle-node-plugin/blob/main/docs/faq.md#is-this-plugin-compatible-with-centralized-repositories-declaration
		ivy {
			name = "Node.js"
			setUrl("https://nodejs.org/dist/")
			patternLayout {
				artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
			}
			metadataSources {
				artifact()
			}
			content {
				includeModule("org.nodejs", "node")
			}
		}
	}
}

pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://redirector.kotlinlang.org/maven/ktor-eap")
	}
}
