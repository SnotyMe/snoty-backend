package me.snoty.backend.test

import kotlinx.coroutines.runBlocking
import me.snoty.backend.utils.HttpStatusException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import kotlin.reflect.KFunction

fun assertErrorResponse(body: JSONObject, exception: HttpStatusException) {
	assertEquals(exception.code.value, body.getInt("code"))
	assertEquals(exception.message, body.getString("message"))
}

inline fun <reified T> assertInstanceOf(actualValue: Any): T
	= assertInstanceOf(T::class.java, actualValue)

fun <R> assertCombinations(
	targetFunction: KFunction<R>,
	vararg parameterOptions: List<Any>,
	exclude: List<Any> = emptyList(),
	assertions: suspend (() -> R) -> Unit
) {
	fun <T> combinations(lists: List<List<T>>): List<List<T>> {
		return lists.fold(listOf(listOf())) { acc, list ->
			acc.flatMap { accItem -> list.map { listItem -> accItem + listItem } }
		}
	}

	val optionsLists = parameterOptions.toList()

	val allCombinations = combinations(optionsLists)

	for (combination in allCombinations) {
		// you can exclude a specific entry
		if (combination == exclude) continue
		runBlocking {
			assertions {
				targetFunction.call(*combination.toTypedArray())
			}
		}
	}
}
