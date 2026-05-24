package me.snoty.integration.plugin.utils

import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.plugin.utils.koin.KoinScopeReferences
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped
import kotlin.reflect.KClass

private const val KOTLINX_SERIALIZATION_PACKAGE = "kotlinx.serialization.modules"

fun TypeSpec.Builder.addSerializersModule(nodes: List<RegisterNode>, extensionName: String, extensionScope: KoinScopeReferences): TypeSpec.Builder {
	val function = providerBuilder(extensionName, extensionScope)
		.returns(SerializersModule::class)
		.addAnnotation(AnnotationSpec.get(OptIn(InternalSerializationApi::class)))
		.addStatement(
			"""
				return %M{
					%M(%T::class) {
						${nodes.joinToString("\n${"\t".repeat(6)}") { "%M(%T::class, %M())" }}
					}
				}
			""".trimIndent(),
			MemberName(KOTLINX_SERIALIZATION_PACKAGE, "SerializersModule"),
			MemberName(KOTLINX_SERIALIZATION_PACKAGE, "polymorphic"),
			NodeSettings::class.asTypeName(),
			*nodes
				.flatMap {
					listOf(
						MemberName(KOTLINX_SERIALIZATION_PACKAGE, "subclass"),
						gimmeTypeName { it.settingsType },
						MemberName("kotlinx.serialization", "serializer"),
					)
				}
				.toTypedArray(),
		)
		.build()
	return addFunction(function)
}

fun providerBuilder(name: String, koinScope: KoinScopeReferences) = FunSpec
	.builder("provide${name.replaceFirstChar { it.uppercase() }}SerializersModule")
	.addAnnotation(Scoped::class)
	.addAnnotation(
		AnnotationSpec.builder(Scope::class)
			.addMember("name = %L", koinScope.scopeValueProperty.name)
			.build()
	)

@OptIn(KspExperimental::class)
fun <T : Any> gimmeTypeName(get: () -> KClass<T>) = runCatching {
	get().asClassName()
}.getOrElse { e ->
	if (e is KSTypeNotPresentException) {
		e.ksType.toClassName()
	} else {
		throw e
	}
}
