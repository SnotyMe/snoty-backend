package me.snoty.backend.database.utils

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.newSuspendedTransaction
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.utils.bson.getIdAsString
import me.snoty.backend.utils.toUuid
import me.snoty.backend.wiring.node.NodesScope
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.diff.checksum
import me.snoty.integration.common.diff.state.EntityState
import me.snoty.integration.common.wiring.Node
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Scope
import kotlin.uuid.Uuid

@Factory
@Scope(NodesScope::class)
class SqlEntityStateService(
	private val db: Database,
	private val codecRegistry: CodecRegistry,
	private val entityStateTable: EntityStateTable,
) : EntityStateService {

	init {
		transaction(db = db) {
			SchemaUtils.create(entityStateTable)
		}
	}

	override suspend fun getLastState(nodeId: NodeId, entityId: String): EntityState? = db.newSuspendedTransaction {
		entityStateTable.selectStandalone()
			.where { (entityStateTable.nodeId eq nodeId.toUuid()) and (entityStateTable.entityId eq entityId) }
			.firstOrNull()
			?.toEntityState(entityStateTable, codecRegistry)
	}

	override fun getLastStates(nodeId: NodeId): Flow<EntityState> = db.flowTransaction {
		entityStateTable.selectStandalone()
			.where { entityStateTable.nodeId eq nodeId.toUuid() }
			.map { it.toEntityState(entityStateTable, codecRegistry) }
	}

	override suspend fun updateState(nodeId: NodeId, state: Document, diff: DiffResult) = db.newSuspendedTransaction {
		doUpdateState(nodeId.toUuid(), state, diff)
	}

	override suspend fun updateStates(
		nodeId: NodeId,
		states: Collection<EntityStateService.EntityStateUpdate>
	) = db.newSuspendedTransaction {
		states.forEach { (state, diffResult) ->
			doUpdateState(nodeId.toUuid(), state.state, diffResult)
		}
	}

	private fun Transaction.doUpdateState(nodeId: Uuid, state: Document, diff: DiffResult) {
		val id = state.getIdAsString() ?: return
		val stateJson by lazy {
			state.toJson(codecRegistry[Document::class.java])
		}
		val checksum by lazy { state.checksum() }

		when (diff) {
			is DiffResult.Created -> entityStateTable.insert {
				it[entityStateTable.nodeId] = nodeId
				it[entityStateTable.entityId] = id
				it[entityStateTable.state] = stateJson
				it[entityStateTable.checksum] = checksum
			}

			is DiffResult.Updated -> entityStateTable.update(where = {
				(entityStateTable.nodeId eq nodeId) and (entityStateTable.entityId eq id)
			}) {
				it[entityStateTable.state] = stateJson
				it[entityStateTable.checksum] = checksum
			}

			is DiffResult.Deleted -> entityStateTable.deleteWhere {
				(entityStateTable.nodeId eq nodeId) and (entityStateTable.entityId eq id)
			}

			is DiffResult.Unchanged -> Unit
		}
		return
	}

	override suspend fun delete(node: Node): Unit = db.newSuspendedTransaction {
		entityStateTable.deleteWhere { entityStateTable.nodeId eq node._id.toUuid() }
	}
}
