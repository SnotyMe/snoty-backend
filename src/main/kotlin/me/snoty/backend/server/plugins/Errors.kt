package me.snoty.backend.server.plugins

suspend fun <T> void(block: suspend () -> Unit): T? {
	block()
	return null
}
