package me.snoty.integration.common.diff

import kotlinx.serialization.json.Json
import me.snoty.integration.common.InstanceId
import me.snoty.backend.utils.When
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

const val ID = "id"
abstract class EntityStateTable<ID>(
	json: Json = Json
) : Table() {
	abstract val id: Column<ID>
	val instanceId = integer("instance_id")

	/**
	 * Override this with `buildPrimaryKey()` to define the primary key
	 * This is required to build the primary key at a point where `id` is already initialized.
	 */
	abstract override val primaryKey: PrimaryKey
	fun buildPrimaryKey() = PrimaryKey(id, instanceId, userId)

	val type = varchar("type", 255)
	val state = jsonb<Fields>("state", json)
	private val checksum = long("checksum")
	val userId = uuid("user_id")

	private fun deleteIdentifier(entity: IUpdatableEntity<ID>, instanceId: InstanceId, userId: UUID): EntityStateTable<ID>.(ISqlExpressionBuilder) -> Op<Boolean> = {
		(id eq entity.id) and (type eq entity.type) and (this.instanceId eq instanceId) and (this.userId eq userId)
	}

	private fun identifier(entity: IUpdatableEntity<ID>, instanceId: InstanceId, userId: UUID): ISqlExpressionBuilder.() -> Op<Boolean> = {
		(id eq entity.id) and (type eq entity.type) and (this@EntityStateTable.instanceId eq instanceId) and (this@EntityStateTable.userId eq userId)
	}

	fun compareState(entity: IUpdatableEntity<ID>, instanceId: InstanceId, userId: UUID): DiffResult {
		val entityChecksum = entity.checksum
		// if the checksum matches, we don't want to query the JSON for performance reasons
		val stateQuery = When(checksum neq entityChecksum, state, null, state.columnType)
		val result = select(checksum, stateQuery)
			.where(identifier(entity, instanceId, userId)).singleOrNull()
				// not found in DB => newly created entity
				?: return DiffResult.Created(entityChecksum, entity.fields)

		// nothing has changed
		if (result[checksum] == entityChecksum) {
			return DiffResult.Unchanged
		}

		// something changed, delegate to entity to determine what
		return entity.diff(result[stateQuery])
	}

	fun compareAndUpdateState(entity: IUpdatableEntity<ID>, instanceId: InstanceId, userId: UUID): DiffResult = transaction {
		val diffResult = compareState(entity, instanceId, userId)
		when (diffResult) {
			// entity has changed since last time
			is DiffResult.Updated -> {
				update(identifier(entity, instanceId, userId)) {
					it[state] = entity.fields
					it[checksum] = entity.checksum
				}
			}
			// entity has never been seen before
			is DiffResult.Created -> {
				insert {
					it[id] = entity.id
					it[this.userId] = userId
					it[this.instanceId] = instanceId
					it[type] = entity.type
					it[state] = entity.fields
					it[checksum] = entity.checksum
				}
			}
			// entity has been deleted
			is DiffResult.Deleted -> {
				deleteWhere(op=deleteIdentifier(entity, instanceId, userId))
			}
			else -> return@transaction diffResult
		}
		return@transaction diffResult
	}
}
