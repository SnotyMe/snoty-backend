package me.snoty.integration.plugin.processor

import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.wiring.node.NodeSettings
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped
import kotlin.reflect.KClass

fun TypeSpec.Builder.addSerializersModule(registerNode: RegisterNode, nodeHandlerScope: TypeSpec): TypeSpec.Builder {
	val serializationModule = "kotlinx.serialization.modules"

	return addFunction(
		providerBuilder(registerNode.name, nodeHandlerScope)
			.returns(SerializersModule::class)
			.addAnnotation(AnnotationSpec.get(OptIn(InternalSerializationApi::class)))
			.addStatement(
				"""
				return %M{
					%M(%T::class) {
						%M(%T::class, %M())
					}
				}
			""".trimIndent(),
				MemberName(serializationModule, "SerializersModule"),
				MemberName(serializationModule, "polymorphic"),
				NodeSettings::class.asTypeName(),
				MemberName(serializationModule, "subclass"),
				gimmeTypeName { registerNode.settingsType },
				MemberName("kotlinx.serialization", "serializer"),
			)
			.build()
	)
}

fun providerBuilder(name: String, nodeHandlerScope: TypeSpec) = FunSpec
	.builder("provide${name.replaceFirstChar { it.uppercase() }}SerializersModule")
	.addAnnotation(Scoped::class)
	.addAnnotation(
		AnnotationSpec.builder(Scope::class)
			.addMember("value = %L::class", nodeHandlerScope.name!!)
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
