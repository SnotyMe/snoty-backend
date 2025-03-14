package me.snoty.backend

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.hooks.impl.PreBusinessStartupHook
import me.snoty.backend.integration.NodeHandlerContributorLookup
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.scheduling.Scheduler
import me.snoty.backend.server.KtorServer
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@Single
class Application : KoinComponent {
	val logger = KotlinLogging.logger {}

	suspend fun start() = coroutineScope {
		// register all node handlers
		get<NodeHandlerContributorLookup>().loadAndRegisterNodeHandlers()

		// application is initialized but no business logic is running yet
		get<HookRegistry>().executeHooks(PreBusinessStartupHook::class, getKoin())

		// now that the application is initialized, start the scheduler
		get<Scheduler>().start()

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
		// this is the last step so health checks only pass once everything is up and running
		get<KtorServer>().start(wait = false)
	}
}
