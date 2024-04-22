package me.snoty.backend.utils

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function

/**
 * Represents a custom SQL function for the SQL 'CASE WHEN'
 * Takes in
 * 1. The condition to check
 * 2. The value to return if the condition is true
 * 3. The value to return if the condition is false
 * 4. The Type of the results
 */
class When<T>(
	private val condition: Expression<*>,
	private val ifTrue: Expression<T>,
	private val ifFalse: Expression<T>?,
	columnType: IColumnType
) : Function<T>(columnType) {
	override fun toQueryBuilder(queryBuilder: QueryBuilder) {
		queryBuilder {
			append("CASE WHEN ")
			append(condition)
			append(" THEN ")
			append(ifTrue)
			if (ifFalse != null) {
				append(" ELSE ")
				append(ifFalse)
			}
			append(" END AS mycase")
		}
	}
}
