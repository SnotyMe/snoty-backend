package me.snoty.backend.scheduling.jobrunr

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.utils.MANAGEMENT_EXECUTOR
import org.jobrunr.configuration.JobRunr
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

@Single
class JobRunrReconnecter(
	@Named(MANAGEMENT_EXECUTOR)
	private val managementPool: ScheduledExecutorService
) {
	private val logger = KotlinLogging.logger {}

	// no other way of getting it unfortunately, the configuration result doesn't expose it
	private fun getBackgroundJobServer() = JobRunr.getBackgroundJobServer()

	private val lock = ReentrantLock()
	private var reconnectThread: Thread? = null

	fun startReconnectLoop() {
		managementPool.scheduleAtFixedRate(
			::checkAndReconnect,
			0,
			30,
			TimeUnit.SECONDS
		)
	}

	fun checkAndReconnect() {
		val backgroundJobServer = getBackgroundJobServer()

		if (backgroundJobServer.isRunning) return

		lock.withLock {
			reconnectThread = when {
				reconnectThread?.isAlive != true -> {
					logger.warn { "JobRunr is stopped, attempting restart..." }

					thread(name = "JobRunr-Restart", start = true) {
						// will try acquiring the JobRunr start lock
						backgroundJobServer.start()
					}
				}

				else -> null // reset if the thread is not alive
			}
		}
	}
}
