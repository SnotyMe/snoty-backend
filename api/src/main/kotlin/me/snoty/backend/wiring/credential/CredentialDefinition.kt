package me.snoty.backend.wiring.credential

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import me.snoty.integration.common.model.metadata.ObjectSchema
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.util.*
import kotlin.reflect.KClass

data class CredentialDefinition(
	val type: String,
	val displayName: String,
	val clazz: Class<out Credential>,
	val schema: ObjectSchema,
)

annotation class RegisterCredential(val type: String, val displayName: String = "")

interface CredentialDefinitionContributor {
	val type: String
	val displayName: String
	val clazz: Class<out Credential>
	/**
	 * Serialized schema as a JSON string
	 */
	val schema: String
}

@OptIn(InternalSerializationApi::class)
@Single
@Named("credentials")
fun provideCredentialSerializersModule() = SerializersModule {
    val contributors = ServiceLoader.load(CredentialDefinitionContributor::class.java)

    polymorphic(Credential::class) {
        for (contributor in contributors) {
            @Suppress("UNCHECKED_CAST")
            val subclass = contributor.clazz.kotlin as KClass<Credential>
            subclass(subclass, subclass.serializer())
        }
    }
}
