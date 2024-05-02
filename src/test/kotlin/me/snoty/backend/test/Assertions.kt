package me.snoty.backend.test

import me.snoty.backend.server.handler.HttpStatusException
import org.json.JSONObject
import org.junit.Assert
import org.junit.function.ThrowingRunnable

inline fun <reified T : Throwable> assertThrows(block: ThrowingRunnable) {
	Assert.assertThrows(T::class.java, block)
}

fun assertErrorResponse(body: JSONObject, exception: HttpStatusException) {
	Assert.assertEquals(exception.code.value, body.getInt("code"))
	Assert.assertEquals(exception.message, body.getString("message"))
}
