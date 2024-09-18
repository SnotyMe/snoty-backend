package me.snoty.backend

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
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

		launch(newSingleThreadContext("NodeScheduler")) {
			val flowService: FlowService = get()
			val flowScheduler: FlowScheduler = get()

			flowService.getAll()
				.catch { e -> logger.error(e) { "Failed to schedule flows" } }
				.collect { flowScheduler.schedule(it) }
		}

		get<KtorServer>().start(wait = true)
	}
}
