package me.snoty.integration.plugin.utils

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.withNullability
import kotlin.reflect.typeOf

// TODO: support generics
fun FileSpec.Builder.addDataClassInitializer(copyFrom: Any, level: Int = 1, replacements: Map<String, Any> = emptyMap()): FileSpec.Builder = apply {
	copyFrom::class.primaryConstructor!!.parameters.forEach { param ->
		val prop = copyFrom::class.memberProperties.first { it.name == param.name }
		val value = prop.call(copyFrom)
		val prefix = "${"\t".repeat(level)}%L = "
		val type = param.type.withNullability(false)
		val isPrimitive = type == typeOf<String>() || type == typeOf<Int>() || type == typeOf<Boolean>()
		addCode(prefix, param.name)
		when {
			replacements.containsKey(param.name) -> addCode("%L", replacements[param.name])
			value == null -> addCode("null")
			isPrimitive -> when {
				type == typeOf<String>() -> addCode("\"%L\"", value)
				else -> addCode("%L", value)
			}
			value::class.isData -> {
				addCode("%T(\n", value::class.asTypeName())
				addDataClassInitializer(value, level + 1)
				addCode("${"\t".repeat(level)})")
			}
			type.isSubtypeOf(typeOf<Enum<*>>()) -> addCode("%T.%L", type.asTypeName(), value)
			(type.isSubtypeOf(typeOf<List<*>>()) || type.isSubtypeOf(typeOf<Array<*>>())) -> {
				addCode("listOf(\n")
				(value as List<*>).filterNotNull().forEach {
					if (it::class.isData) {
						addCode("${"\t".repeat(level + 1)}%T(\n", it::class.asTypeName())
						addDataClassInitializer(it, level + 2)
						addCode("${"\t".repeat(level+1)})")
					} else {
						addDataClassInitializer(it, level + 2)
					}
					addCode(",\n")
				}
				addCode("${"\t".repeat(level)})")
			}
			value is KClass<*> -> addCode("%T::class", value)
			else -> throw IllegalArgumentException("Unsupported type: ${type.asTypeName()}")
		}
		addCode(",\n")
	}
}

fun String.quoted() = "\"$this\""
