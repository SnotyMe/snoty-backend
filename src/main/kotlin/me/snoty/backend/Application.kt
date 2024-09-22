package me.snoty.backend

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import me.snoty.backend.featureflags.FeatureFlagsSetup
import me.snoty.backend.integration.NodeHandlerContributorLookup
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.server.KtorServer
import org.koin.core.Koin

class Application(val koin: Koin) {
	val logger = KotlinLogging.logger {}

	inline fun <reified T : Any> get() = koin.get<T>()

	suspend fun start() = coroutineScope {
		FeatureFlagsSetup.setup(get(), get())

		NodeHandlerContributorLookup(koin).executeContributors()

		launch(
			newSingleThreadContext("FlowScheduler") +
				SupervisorJob() +
				CoroutineExceptionHandler { _, err ->
					logger.error(err) { "Exception scheduling flows" }
				}
		) {
			get<FlowScheduler>().scheduleMissing(get())
		}

		get<KtorServer>().start(wait = false)
	}
}
