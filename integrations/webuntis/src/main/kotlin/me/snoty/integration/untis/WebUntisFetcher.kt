package me.snoty.integration.untis

import me.snoty.integration.common.NodeHandlerContext
import me.snoty.integration.common.fetch.AbstractIntegrationFetcher
import me.snoty.integration.common.fetch.FetchContext
import me.snoty.integration.common.httpClient
import me.snoty.integration.common.wiring.EdgeVertex
import me.snoty.integration.common.wiring.IFlowNode
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.untis.request.getExams
import org.jobrunr.jobs.context.JobContext
import org.jobrunr.jobs.context.JobRunrDashboardLogger

open class WebUntisFetcher(
	private val nodeHandlerContext: NodeHandlerContext,
	private val untisAPI: WebUntisAPI = WebUntisAPIImpl(nodeHandlerContext.httpClient())
) : AbstractIntegrationFetcher() {
	override val position = NodePosition.START
	override val settingsClass = WebUntisSettings::class

	private suspend fun FetchContext.fetchExams(
		node: IFlowNode,
		logger: JobRunrDashboardLogger,
	) = nodeHandlerContext.run {
		val untisSettings = node.getConfig<WebUntisSettings>(codecRegistry)

		val exams = fetchStage {
			untisAPI.getExams(untisSettings)
		}

		updateStage {
			entityStateService.updateStates(node, exams)
		}

		logger.info("Fetched ${exams.size} exams for ${untisSettings.username}")

		return@run exams
	}

	override suspend fun process(node: IFlowNode, input: EdgeVertex): EdgeVertex {
		val jobContext = input as JobContext
		val progress = progress(jobContext, 1)

		return progress.fetchExams(node, logger)
	}
}
