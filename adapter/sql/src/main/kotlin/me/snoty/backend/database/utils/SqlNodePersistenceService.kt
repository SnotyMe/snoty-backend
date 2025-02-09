package me.snoty.backend.database.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.newSuspendedTransaction
import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.hooks.register
import me.snoty.backend.utils.hackyEncodeToString
import me.snoty.backend.utils.toUuid
import me.snoty.backend.wiring.node.NodeTable
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.flow.NodeDeletedHook
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePersistenceFactory
import me.snoty.integration.common.wiring.node.NodePersistenceService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.KClass

class SqlNodePersistenceService<T : Any>(
	private val db: Database,
	private val entityClass: KClass<T>,
	descriptor: NodeDescriptor,
	name: String,
	nodeTable: NodeTable,
) : NodePersistenceService<T>, KoinComponent {
	private val nodePersistenceTable = NodePersistenceTable<T>(descriptor, name, nodeTable)
	// lazy to collect all settings classes before constructing the Json object
	private val json: Json by inject()

	init {
		transaction(db = db) {
			SchemaUtils.create(nodePersistenceTable)
		}
	}

	override suspend fun persistEntity(node: Node, entityId: String, entity: T) {
		nodePersistenceTable.upsert {
			it[id] = node._id.toUuid()
			it[this.entityId] = entityId
			it[this.entity] = json.hackyEncodeToString(entity)
		}
	}

	override suspend fun setEntities(node: Node, entities: List<T>, idGetter: (T) -> String): Unit = db.newSuspendedTransaction {
		nodePersistenceTable.deleteWhere { nodePersistenceTable.id eq node._id.toUuid() }
		nodePersistenceTable.batchInsert(entities) { entity ->
			this[nodePersistenceTable.id] = node._id.toUuid()
			this[nodePersistenceTable.entityId] = idGetter(entity)
			this[nodePersistenceTable.entity] = json.hackyEncodeToString(entity)
		}
	}

	@OptIn(InternalSerializationApi::class)
	override fun getEntities(node: Node): Flow<T> = db.flowTransaction {
		nodePersistenceTable.select(nodePersistenceTable.entity)
			.where { nodePersistenceTable.id eq node._id.toUuid() }
			.map { json.decodeFromString(entityClass.serializer(), it[nodePersistenceTable.entity]) }
	}

	override suspend fun deleteEntity(node: Node, entityId: String): Unit = db.newSuspendedTransaction {
		nodePersistenceTable.deleteWhere {
			(nodePersistenceTable.id eq node._id.toUuid()) and (nodePersistenceTable.entityId eq entityId)
		}
	}

	override suspend fun delete(node: Node): Unit = db.newSuspendedTransaction {
		nodePersistenceTable.deleteWhere { nodePersistenceTable.id eq node._id.toUuid() }
	}
}

@Factory
class SqlNodePersistenceFactory(
	private val database: Database,
	private val hookRegistry: HookRegistry,
	private val nodeDescriptor: NodeDescriptor,
	private val nodeTable: NodeTable,
) : NodePersistenceFactory, KoinComponent {
	override fun <T : Any> create(name: String, entityClass: KClass<T>): NodePersistenceService<T> {
		val service = SqlNodePersistenceService(database, entityClass, nodeDescriptor, name, nodeTable)
		hookRegistry.register(NodeDeletedHook {
			service.delete(it)
		})
		return service
	}
}
