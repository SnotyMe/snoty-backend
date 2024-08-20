package me.snoty.integration.common.wiring.node

import kotlinx.coroutines.flow.Flow
import me.snoty.integration.common.wiring.Node
import kotlin.reflect.KClass

interface NodePersistenceService<T : Any> {
	suspend fun persistEntity(node: Node, entityId: String, entity: T)

	fun getEntities(node: Node): Flow<T>
}

interface NodePersistenceServiceFactory {
	fun <T : Any> create(name: String, entityClass: KClass<T>): NodePersistenceService<T>
}

inline fun <reified T : Any> NodeHandler.persistenceService(name: String) =
	nodeHandlerContext.nodePersistenceServiceFactory.create<T>(name, T::class)
