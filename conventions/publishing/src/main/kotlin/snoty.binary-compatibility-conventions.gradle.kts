import org.jetbrains.kotlin.gradle.dsl.abi.BinariesSource
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
	kotlin("jvm")
}

kotlin {
	@OptIn(ExperimentalAbiValidation::class)
	abiValidation {
		binariesSource.set(BinariesSource.MAVEN_PUBLICATIONS)
		referenceDumpDir.convention(this@kotlin.project.layout.projectDirectory)

		filters {
			exclude {
				byNames.addAll(
					$$$"**$serializer",

					"org.koin.plugin.hints.**",
				)
			}
		}
	}
}
