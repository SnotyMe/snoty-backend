package me.snoty.backend.integration.common.diff

import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import me.snoty.backend.integration.common.InstanceId
import me.snoty.backend.utils.When
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.json.jsonb

const val ID = "id"
abstract class EntityStateTable<ID>(
	json: Json = DefaultJson
) : Table() {
	abstract val id: Column<ID>
	private val instanceId = integer("instance_id")

	/**
	 * Override this with `buildPrimaryKey()` to define the primary key
	 * This is required to build the primary key at a point where `id` is already initialized.
	 */
	abstract override val primaryKey: PrimaryKey
	fun buildPrimaryKey() = PrimaryKey(id, instanceId)

	private val type = varchar("type", 255)
	private val state = jsonb<Fields>("state", json)
	private val checksum = long("checksum")

	private fun deleteIdentifier(instanceId: InstanceId, entity: IUpdatableEntity<ID>): EntityStateTable<ID>.(ISqlExpressionBuilder) -> Op<Boolean> = {
		(id eq entity.id) and (type eq entity.type) and (this.instanceId eq instanceId)
	}

	private fun identifier(instanceId: InstanceId, entity: IUpdatableEntity<ID>): ISqlExpressionBuilder.() -> Op<Boolean> = {
		(id eq entity.id) and (type eq entity.type) and (this@EntityStateTable.instanceId eq instanceId)
	}

	fun compareState(instanceId: InstanceId, entity: IUpdatableEntity<ID>): DiffResult {
		val entityChecksum = entity.checksum
		// if the checksum matches, we don't want to query the JSON for performance reasons
		val stateQuery = When(checksum neq entityChecksum, state, null, state.columnType)
		val state = select(checksum, stateQuery)
			.where(identifier(instanceId, entity)).singleOrNull()?.let {
				// nothing has changed
				if (it[checksum] == entityChecksum) {
					return DiffResult.NoChange
				}
				// in this case, the JSON should be non-null
				return@let it[stateQuery]
			} ?: return DiffResult.Created(entityChecksum, entity.fields)

		// something changed, delegate to entity to determine what
		return entity.diff(state)
	}

	fun compareAndUpdateState(instanceId: InstanceId, entity: IUpdatableEntity<ID>): DiffResult {
		val diffResult = compareState(instanceId, entity)
		when (diffResult) {
			// entity has changed since last time
			is DiffResult.Updated -> {
				update(identifier(instanceId, entity)) {
					it[state] = entity.fields
					it[checksum] = entity.checksum
				}
			}
			// entity has never been seen before
			is DiffResult.Created -> {
				insert {
					it[id] = entity.id
					it[this.instanceId] = instanceId
					it[type] = entity.type
					it[state] = entity.fields
					it[checksum] = entity.checksum
				}
			}
			// entity has been deleted
			is DiffResult.Deleted -> {
				deleteWhere(op=deleteIdentifier(instanceId, entity))
			}
			else -> return diffResult
		}
		return diffResult
	}
}
