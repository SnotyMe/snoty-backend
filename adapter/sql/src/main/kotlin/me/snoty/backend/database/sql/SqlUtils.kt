package me.snoty.backend.database.sql

import me.snoty.backend.utils.flowOf
import me.snoty.backend.utils.flowOfEach
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend inline fun <T> Database.newSuspendedTransaction(noinline statement: suspend Transaction.() -> T)
	= newSuspendedTransaction(db = this, statement = statement)

fun <T> Database.flowTransaction(statement: suspend Transaction.() -> T) = flowOf<T> {
	this.newSuspendedTransaction(statement)
}

@JvmName("flowTransactionCollection")
fun <T> Database.flowTransaction(statement: suspend Transaction.() -> Collection<T>) = flowOfEach<T> {
	this.newSuspendedTransaction(statement)
}
