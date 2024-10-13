package me.snoty.backend.database.mongo.tracing

import com.mongodb.RequestContext
import java.util.stream.Stream

class RequestContextMap : RequestContext {
	private val map = mutableMapOf<Any, Any>()

	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> get(key: Any): T = map[key] as T

	override fun hasKey(key: Any): Boolean = map.containsKey(key)

	override fun isEmpty(): Boolean = map.isEmpty()

	override fun put(key: Any, value: Any) {
		map[key] = value
	}

	override fun delete(key: Any) {
		map.remove(key)
	}

	override fun size(): Int = map.size

	override fun stream(): Stream<MutableMap.MutableEntry<Any, Any>> = map.entries.stream()
}

inline fun <reified T : Any> RequestContext.get(): T? = get(T::class) ?: get(T::class.java)
inline fun <reified T : Any> RequestContext.put(value: T): Unit = put(T::class, value)
inline fun <reified T : Any> RequestContext.delete(): Unit = delete(T::class)
