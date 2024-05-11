package me.snoty.backend.integration.common.diff

/**
 * Represents an entity that can be updated.
 * An entity can be an exam, assignment, user, etc.
 * Entities are immutable and can be updated by creating a new instance with the updated values.
 */
interface IUpdatableEntity<ID>{
	val id: ID
	val type: String
	val fields: Fields
	val checksum: Long
	fun diff(other: Fields): DiffResult
}

/**
 * Superclass providing abstractions updatable entities.
 * It is recommended to extend this class when creating new updatable entities.
 */
abstract class UpdatableEntity<ID> : IUpdatableEntity<ID> {
	override val checksum: Long by lazy { fields.checksum() }

	override fun diff(other: Fields): DiffResult {
		val diff: Diff = fields.entries
			.filter { (key, value) -> value != other[key] }
			.associate { (key, value) -> (key to OldNew(other[key] as Any, value)) }

		return when {
			diff.isEmpty() -> DiffResult.Unchanged
			else -> DiffResult.Updated(checksum, diff)
		}
	}
}
