package me.snoty.backend

import me.snoty.backend.build.BuildInfo
import me.snoty.backend.config.Config
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.ConfigLoaderImpl
import me.snoty.backend.server.KtorServer
import me.snoty.backend.utils.getKoinInstance
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.logger.slf4jLogger

fun main() {
	val appModule = module {
		single<ConfigLoader> { ConfigLoaderImpl() }
		single<Config> { getKoinInstance<ConfigLoader>().loadConfig() }
		single<BuildInfo> { getKoinInstance<ConfigLoader>().loadBuildInfo() }
	}

	startKoin {
		modules(appModule)
		slf4jLogger()

		KtorServer()
			.start(wait = true)
	}
}
