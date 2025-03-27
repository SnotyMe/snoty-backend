package me.snoty.backend.database.sql

import me.snoty.backend.utils.flowOfEach
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.withSuspendTransaction
import org.jetbrains.exposed.sql.transactions.transactionManager

suspend inline fun <T> Database.newSuspendedTransaction(noinline statement: suspend Transaction.() -> T)
	= transactionManager.currentOrNull()?.withSuspendTransaction(statement = statement)
		?: newSuspendedTransaction(db = this, statement = statement)

@JvmName("flowTransactionCollection")
fun <T> Database.flowTransaction(statement: suspend Transaction.() -> Collection<T>) = flowOfEach {
	this.newSuspendedTransaction(statement)
}
