package me.snoty.integration.plugin.utils

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName

data class SpiContributor(val contributorClassName: ClassName, val containingFile: KSFile)

fun CodeGenerator.writeSpiFile(serviceQualifiedName: String, services: List<SpiContributor>) {
	createNewFileByPath(
		dependencies = Dependencies(
			aggregating = true,
			*(services.map { it.containingFile }.toTypedArray())
		),
		path = "META-INF/services/${serviceQualifiedName}",
		extensionName = ""
	).writer().use {
		services.forEach { contributor ->
			it.appendLine(contributor.contributorClassName.canonicalName)
		}
	}
}
