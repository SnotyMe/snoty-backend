package me.snoty.integration.common.wiring.node

import me.snoty.integration.common.model.metadata.NodeMetadata
import org.koin.core.module.Module
import kotlin.reflect.KClass

interface NodeHandlerContributor {
	val metadata: NodeMetadata
	val nodeHandlerClass: KClass<out NodeHandler>
	val koinModules: List<Module>
}
