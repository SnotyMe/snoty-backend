package me.snoty.backend.authentication

import kotlinx.coroutines.runBlocking
import me.snoty.backend.adapter.Adapter
import org.koin.core.Koin
import org.koin.dsl.module

interface AuthenticationAdapter : Adapter {
	data class OnBuildAuthenticationMetadata(
		val koin: Koin,
	)
	suspend fun buildAuthenticationMetadata(event: OnBuildAuthenticationMetadata): AuthenticationMetadata

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
