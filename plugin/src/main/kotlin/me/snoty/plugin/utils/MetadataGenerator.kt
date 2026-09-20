package me.snoty.plugin.utils

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import me.snoty.backend.metadata.Icon
import me.snoty.backend.wiring.node.NodeSettings
import me.snoty.backend.wiring.node.ReceiveEmptyInput
import me.snoty.backend.wiring.node.RegisterNode
import me.snoty.backend.wiring.node.metadata.NodeMetadata
import me.snoty.backend.wiring.node.metadata.metadataJson
import me.snoty.core.node.NodeType

fun generateMetadata(resolver: Resolver, clazz: KSClassDeclaration, node: RegisterNode): String {
	val settingsClass = resolver.resolveClassFromAnnotation(clazz, RegisterNode::settingsType)
	val inputClass = resolver.resolveClassFromAnnotation(clazz, RegisterNode::inputType)
	val outputClass = resolver.resolveClassFromAnnotation(clazz, RegisterNode::outputType)
	val metadata = NodeMetadata(
		type = NodeType(node.name),
		displayName = node.displayName,
		icon = node.icon
			.takeIf { it.name.isNotEmpty() }
			?.let(Icon::of),
		stereotype = node.stereotype,
		settingsClass = NodeSettings::class,
		settings = generateObjectSchema(resolver, settingsClass)!!,
		input = generateObjectSchema(resolver, inputClass),
		receiveEmptyInput = clazz.hasAnnotation<ReceiveEmptyInput>(),
		output = generateObjectSchema(resolver, outputClass),
	)

	return metadataJson.encodeToString(metadata)
}
