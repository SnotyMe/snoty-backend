package me.snoty.integration.common.diff

import org.bson.Document

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

	/**
	 * Executed before calculating the diff.
	 * Can be used to transform Strings to LocalDateTime etc.
	 */
	fun prepareFieldsForDiff(fields: Fields) {
		// NOOP
	}
}

/**
 * Superclass providing abstractions updatable entities.
 * It is recommended to extend this class when creating new updatable entities.
 */
abstract class UpdatableEntity<ID> : IUpdatableEntity<ID> {
	override val checksum: Long by lazy { fields.checksum() }

	override fun diff(other: Fields): DiffResult {
		prepareFieldsForDiff(other)
		val diff: Diff = fields.entries
			.filter { (key, value) -> value != other[key] }
			.associate { (key, value) ->
				try {
					(key to Change(value, other[key]!!))
				} catch (e: ClassCastException) {
					throw ClassCastException("Mismatch in type! Currently inspecting `${this@UpdatableEntity.type}`. ${e.message}")
				}
			}

		return when {
			diff.isEmpty() -> DiffResult.Unchanged
			else -> DiffResult.Updated(checksum, diff)
		}
	}

	protected fun buildDocument(block: Document.() -> Unit) = Document().apply(block)
}
