package me.snoty.integration.common.wiring.node

import kotlinx.coroutines.flow.Flow
import me.snoty.integration.common.wiring.Node
import kotlin.reflect.KClass

interface NodePersistenceService<T : Any> {
	suspend fun persistEntity(node: Node, entityId: String, entity: T)

	fun getEntities(node: Node): Flow<T>
}

interface NodePersistenceFactory {
	fun <T : Any> create(nodeDescriptor: NodeDescriptor, name: String, entityClass: KClass<T>): NodePersistenceService<T>
}

context(NodeHandler)
inline operator fun <reified T : Any> NodePersistenceFactory.invoke(name: String) = create(
	nodeDescriptor = metadata.descriptor,
	name = name,
	entityClass = T::class
)
