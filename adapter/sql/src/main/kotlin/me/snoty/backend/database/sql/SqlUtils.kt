package me.snoty.backend.database.sql

import com.zaxxer.hikari.HikariDataSource
import me.snoty.backend.utils.flowOfEach
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import javax.sql.DataSource

const val DEFAULT_SCHEMA = "public"

val DataSource.schema: String
	get() = unwrap(HikariDataSource::class.java).schema ?: DEFAULT_SCHEMA

suspend inline fun <T> Database.suspendTransaction(noinline statement: suspend Transaction.() -> T)
	= suspendTransaction(db = this, statement = statement)

@JvmName("flowTransactionCollection")
fun <T> Database.flowTransaction(statement: suspend Transaction.() -> Collection<T>) = flowOfEach {
	this.suspendTransaction(statement)
}
