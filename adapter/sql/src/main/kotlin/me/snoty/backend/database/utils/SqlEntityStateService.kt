package me.snoty.backend.database.utils

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.suspendTransaction
import me.snoty.backend.utils.bson.getIdAsString
import me.snoty.core.Node
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.diff.checksum
import me.snoty.integration.common.diff.state.EntityState
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.annotation.Factory

@Factory
class SqlEntityStateService(
	private val db: Database,
	private val codecRegistry: CodecRegistry,
	private val entityStateTable: EntityStateTable,
) : EntityStateService {
	override suspend fun getLastState(node: Node, entityId: String): EntityState? = db.suspendTransaction {
		entityStateTable.selectStandalone()
			.where { (entityStateTable.nodeId eq node.id) and (entityStateTable.entityId eq entityId) }
			.firstOrNull()
			?.toEntityState(entityStateTable, codecRegistry)
	}

	override fun getLastStates(node: Node): Flow<EntityState> = db.flowTransaction {
		entityStateTable.selectStandalone()
			.where { entityStateTable.nodeId eq node.id }
			.map { it.toEntityState(entityStateTable, codecRegistry) }
	}

	override suspend fun updateState(node: Node, state: Document, diff: DiffResult) = db.suspendTransaction {
		doUpdateState(node, state, diff)
	}

	override suspend fun updateStates(
		node: Node,
		states: Collection<EntityStateService.EntityStateUpdate>
	) = db.suspendTransaction {
		states.forEach { (state, diffResult) ->
			doUpdateState(node, state.state, diffResult)
		}
	}

	private fun Transaction.doUpdateState(node: Node, state: Document, diff: DiffResult) {
		val id = state.getIdAsString() ?: return
		val stateJson by lazy {
			state.toJson(codecRegistry[Document::class.java])
		}
		val checksum by lazy { state.checksum() }

		when (diff) {
			is DiffResult.Created -> entityStateTable.insert {
				it[entityStateTable.nodeId] = node.id
				it[entityStateTable.entityId] = id
				it[entityStateTable.state] = stateJson
				it[entityStateTable.checksum] = checksum
			}

			is DiffResult.Updated -> entityStateTable.update(where = {
				(entityStateTable.nodeId eq node.id) and (entityStateTable.entityId eq id)
			}) {
				it[entityStateTable.state] = stateJson
				it[entityStateTable.checksum] = checksum
			}

			is DiffResult.Deleted -> entityStateTable.deleteWhere {
				(entityStateTable.nodeId eq node.id) and (entityStateTable.entityId eq id)
			}

			is DiffResult.Unchanged -> Unit
		}
		return
	}

	override suspend fun delete(node: Node): Unit = db.suspendTransaction {
		entityStateTable.deleteWhere { entityStateTable.nodeId eq node.id }
	}
}
