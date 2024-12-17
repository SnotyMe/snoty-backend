package me.snoty.backend

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import me.snoty.backend.featureflags.FeatureFlagsSetup
import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.hooks.impl.PreBusinessStartupHook
import me.snoty.backend.integration.NodeHandlerContributorLookup
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.server.KtorServer
import org.koin.core.Koin

class Application(val koin: Koin) {
	val logger = KotlinLogging.logger {}

	inline fun <reified T : Any> get() = koin.get<T>()

	suspend fun start() = coroutineScope {
		FeatureFlagsSetup.setup(get(), koin.getAll())

		// register all node handlers
		get<NodeHandlerContributorLookup>().executeContributors()

		// application is initialized but no business logic is running yet
		// TODO: verify Jobrunr didn't start in the background
		get<HookRegistry>().executeHooks(PreBusinessStartupHook::class, koin)

		// schedule missing jobs
		launch(
			newSingleThreadContext("FlowScheduler") +
				SupervisorJob() +
				CoroutineExceptionHandler { _, err ->
					logger.error(err) { "Exception scheduling flows" }
				}
		) {
			get<FlowScheduler>().scheduleMissing(get())
		}

		// final step: running the ktor server
		get<KtorServer>().start(wait = false)
	}
}
