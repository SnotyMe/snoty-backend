package me.snoty.integration.untis

import me.snoty.backend.utils.contextual
import me.snoty.integration.common.NodeContext
import me.snoty.integration.common.fetch.AbstractIntegrationFetcher
import me.snoty.integration.common.fetch.FetchContext
import me.snoty.integration.common.wiring.EdgeVertex
import me.snoty.integration.common.wiring.EdgeVertices
import me.snoty.integration.common.wiring.IFlowNode
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.untis.request.getExams
import org.jobrunr.jobs.context.JobContext
import org.jobrunr.jobs.context.JobDashboardLogger

open class WebUntisFetcher(
	private val nodeContext: NodeContext,
	private val untisAPI: WebUntisAPI = WebUntisAPIImpl()
) : AbstractIntegrationFetcher() {
	override val position = NodePosition.START

	private suspend fun FetchContext.fetchExams(
		node: IFlowNode,
		logger: JobDashboardLogger,
	) = nodeContext.contextual {
		val untisSettings = node.getConfig<WebUntisSettings>(codecRegistry)

		val exams = fetchStage {
			untisAPI.getExams(untisSettings)
		}

		updateStage {
			entityStateService.updateStates(node, exams)
		}

		logger.info("Fetched ${exams.size} exams for ${untisSettings.username}")

		flowStage {
			flowService.runFlow(node, exams)
		}
	}

	override suspend fun process(node: IFlowNode, input: EdgeVertex): EdgeVertex {
		val jobContext = input as JobContext
		val logger = logger(jobContext)
		val progress = progress(jobContext, 1)

		progress.fetchExams(node, logger)

		return EdgeVertices.START_OF_FLOW
	}
}
