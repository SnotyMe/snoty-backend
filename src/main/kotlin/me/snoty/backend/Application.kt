package me.snoty.backend

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.newSingleThreadContext
import me.snoty.backend.featureflags.FeatureFlagsSetup
import me.snoty.backend.integration.NodeHandlerContributorLookup
import me.snoty.backend.scheduling.NodeScheduler
import me.snoty.backend.server.KtorServer
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.model.NodePosition
import org.koin.core.Koin

class Application(val koin: Koin) {
	val logger = KotlinLogging.logger {}

	inline fun <reified T : Any> get() = koin.get<T>()

	suspend fun start() {
		FeatureFlagsSetup.setup(get(), get())

		val nodeService: NodeService = get()

		NodeHandlerContributorLookup(koin).executeContributors()

		newSingleThreadContext("NodeScheduler").use {
			nodeService.query(position = NodePosition.START)
				.collect(get<NodeScheduler>()::schedule)
		}

		get<KtorServer>().start(wait = true)
	}
}
