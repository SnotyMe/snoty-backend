package me.snoty.backend.authentication

import io.ktor.openapi.*
import kotlinx.coroutines.runBlocking
import me.snoty.backend.adapter.Adapter
import me.snoty.backend.adapter.primaryType
import org.koin.core.Koin
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

interface AuthenticationAdapter : Adapter {
	data class OnBuildAuthenticationMetadata(
		val koin: Koin,
	)
	suspend fun buildAuthenticationMetadata(event: OnBuildAuthenticationMetadata): AuthenticationMetadata
	fun getMetadataJsonSchema(jsonSchemaInference: JsonSchemaInference): JsonSchema

	override fun registerAlwaysOn(event: Adapter.OnRegisterAlwaysOn) {
		val jsonSchema = getMetadataJsonSchema(event.koin.get())
			.copy(
				title = primaryType.replaceFirstChar(Char::uppercase) + AuthenticationMetadata::class.simpleName!!,
				discriminator = JsonSchemaDiscriminator(AuthenticationMetadata::class.simpleName!!),
			)

		val module = module {
			scope(StringQualifier(primaryType)) {
				scoped { jsonSchema }
			}
		}

		event.koin.loadModules(listOf(module))
	}

	override fun onLoad(event: Adapter.OnLoad) {
		val metadata = runBlocking {
			buildAuthenticationMetadata(OnBuildAuthenticationMetadata(event.koin))
		}

		event.koin.loadModules(listOf(
			module {
				single { metadata }
			}
		))
	}

    companion object {
		const val CONFIG_GROUP = "authentication"
	}
}
