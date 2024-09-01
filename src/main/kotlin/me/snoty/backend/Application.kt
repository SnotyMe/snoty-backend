package me.snoty.backend

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.newSingleThreadContext
import me.snoty.backend.featureflags.FeatureFlagsSetup
import me.snoty.backend.integration.NodeHandlerContributorLookup
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.server.KtorServer
import me.snoty.integration.common.wiring.flow.FlowService
import org.koin.core.Koin

class Application(val koin: Koin) {
	val logger = KotlinLogging.logger {}

	inline fun <reified T : Any> get() = koin.get<T>()

	suspend fun start() = coroutineScope {
		FeatureFlagsSetup.setup(get(), get())

		NodeHandlerContributorLookup(koin).executeContributors()

		async(newSingleThreadContext("NodeScheduler")) {
			val flowService: FlowService = get()
			val flowScheduler: FlowScheduler = get()

			flowService.getAll()
				.collect { flowScheduler.schedule(it) }
		}

		get<KtorServer>().start(wait = true)
	}
}
