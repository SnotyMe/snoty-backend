package me.snoty.backend.scheduling

import com.sun.net.httpserver.HttpContext
import org.jobrunr.dashboard.JobRunrApiHandler
import org.jobrunr.dashboard.JobRunrDashboardWebServer
import org.jobrunr.dashboard.JobRunrSseHandler
import org.jobrunr.dashboard.JobRunrStaticFileHandler
import org.jobrunr.dashboard.server.HttpExchangeHandler
import org.jobrunr.dashboard.server.WebServer
import org.jobrunr.dashboard.server.http.RedirectHttpHandler
import org.jobrunr.storage.StorageProvider
import org.jobrunr.storage.ThreadSafeStorageProvider
import org.jobrunr.utils.annotations.VisibleFor
import org.jobrunr.utils.mapper.JsonMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Custom JobRunrDashboardWebServer that binds to 0.0.0.0
 */
class JobRunrDashboardWebServerOverride(storageProvider: StorageProvider, private val jsonMapper: JsonMapper, private val port: Int)
	: JobRunrDashboardWebServer(storageProvider, jsonMapper, port) {
	private val storageProvider: StorageProvider = ThreadSafeStorageProvider(storageProvider)
	private var webServer: WebServer? = null

	override fun start() {
		val redirectHttpHandler = RedirectHttpHandler("/", "/dashboard")
		val staticFileHandler = createStaticFileHandler()
		val dashboardHandler = createApiHandler(storageProvider, jsonMapper, false)
		val sseHandler = createSSeHandler(storageProvider, jsonMapper)

		webServer = object : WebServer(port) {
			override fun getWebServerHostAddress(): String {
				return "0.0.0.0"
			}
		}
		registerContext(redirectHttpHandler)
		registerContext(staticFileHandler)
		registerContext(dashboardHandler)
		registerContext(sseHandler)
		webServer!!.start()

		LOGGER.info(
			"JobRunr Dashboard using {} started at http://{}:{}/dashboard",
			storageProvider.storageProviderInfo.name,
			webServer!!.webServerHostAddress,
			webServer!!.webServerHostPort
		)
	}

	fun registerContext(httpHandler: HttpExchangeHandler?): HttpContext {
		return webServer!!.createContext(httpHandler)
	}

	@VisibleFor("github issue 18")
	fun createStaticFileHandler(): JobRunrStaticFileHandler {
		return JobRunrStaticFileHandler()
	}

	@VisibleFor("github issue 18")
	fun createApiHandler(storageProvider: StorageProvider?, jsonMapper: JsonMapper?, allowAnonymousDataUsage: Boolean): JobRunrApiHandler {
		return JobRunrApiHandler(storageProvider, jsonMapper, allowAnonymousDataUsage)
	}

	@VisibleFor("github issue 18")
	fun createSSeHandler(storageProvider: StorageProvider?, jsonMapper: JsonMapper?): JobRunrSseHandler {
		return JobRunrSseHandler(storageProvider, jsonMapper)
	}

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(JobRunrDashboardWebServer::class.java)
	}
}
