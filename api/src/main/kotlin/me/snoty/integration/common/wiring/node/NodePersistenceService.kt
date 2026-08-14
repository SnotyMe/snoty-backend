package me.snoty.integration.common.wiring.node

import kotlinx.coroutines.flow.Flow
import me.snoty.core.node.Node
import kotlin.reflect.KClass

interface NodePersistenceService<T : Any> : NodeScopedPersistenceService {
	suspend fun persistEntity(node: Node, entityId: String, entity: T)
	suspend fun setEntities(node: Node, entities: List<T>, idGetter: (T) -> String)

	fun getEntities(node: Node): Flow<T>

	suspend fun deleteEntity(node: Node, entityId: String)
}

interface NodePersistenceFactory {
	fun <T : Any> create(name: String, entityClass: KClass<T>): NodePersistenceService<T>
}

inline operator fun <reified T : Any> NodePersistenceFactory.invoke(name: String) = create(
	name = name,
	entityClass = T::class,
)
