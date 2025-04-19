package me.snoty.integration.plugin.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import me.snoty.backend.wiring.node.metadataJson
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.node.NodeSettings

@OptIn(KspExperimental::class)
fun generateMetadata(resolver: Resolver, clazz: KSClassDeclaration, node: RegisterNode): String {
	val settingsClass = resolver.resolveClassFromAnnotation(clazz, RegisterNode::settingsType)
	val inputClass = resolver.resolveClassFromAnnotation(clazz, RegisterNode::inputType)
	val outputClass = resolver.resolveClassFromAnnotation(clazz, RegisterNode::outputType)
	val metadata = NodeMetadata(
		descriptor = node.descriptor(clazz),
		displayName = node.displayName,
		position = node.position,
		settingsClass = NodeSettings::class,
		settings = generateObjectSchema(resolver, settingsClass)!!,
		input = generateObjectSchema(resolver, inputClass),
		output = generateObjectSchema(resolver, outputClass)
	)

	return metadataJson.encodeToString(metadata)
}
