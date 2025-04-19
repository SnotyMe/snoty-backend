package me.snoty.integration.common.wiring.node

import me.snoty.integration.common.model.metadata.NodeMetadata
import org.koin.core.module.Module
import kotlin.reflect.KClass

interface NodeHandlerContributor {
	@Deprecated("Use NodeHandlerContributor.metadataV2 instead")
	val metadata: NodeMetadata?
		get() = null

	val metadataV2: String?
		get() = null
	val settingsClass: KClass<out NodeSettings>?
		get() = null

	val nodeHandlerClass: KClass<out NodeHandler>
	val koinModules: List<Module>
}
