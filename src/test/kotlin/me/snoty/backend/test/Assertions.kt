package me.snoty.backend.test

import me.snoty.backend.utils.HttpStatusException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals

fun assertErrorResponse(body: JSONObject, exception: HttpStatusException) {
	assertEquals(exception.code.value, body.getInt("code"))
	assertEquals(exception.message, body.getString("message"))
}
