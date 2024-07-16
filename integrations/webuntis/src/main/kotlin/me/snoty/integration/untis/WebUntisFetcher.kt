package me.snoty.integration.untis

import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.fetch.AbstractIntegrationFetcher
import me.snoty.integration.common.fetch.FetchContext
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.*
import me.snoty.integration.common.wiring.data.EmitNodeOutputContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.untis.model.UntisExam
import me.snoty.integration.untis.request.getExams
import org.jobrunr.jobs.context.JobContext
import org.slf4j.Logger

@RegisterNode(
	displayName = "WebUntis",
	type = "webuntis",
	position = NodePosition.START,
	settingsType = WebUntisSettings::class
)
open class WebUntisFetcher(
	override val nodeHandlerContext: NodeHandlerContext,
	private val untisAPI: WebUntisAPI = WebUntisAPIImpl(nodeHandlerContext.httpClient())
) : AbstractIntegrationFetcher() {
	override val settingsClass = WebUntisSettings::class

	context(FetchContext, NodeHandlerContext)
	private suspend fun fetchExams(
		node: Node,
		logger: Logger,
	): List<UntisExam> {
		val untisSettings = node.getConfig<WebUntisSettings>()

		val exams = fetchStage {
			untisAPI.getExams(untisSettings)
		}

		updateStage {
			entityStateService.updateStates(node, exams)
		}

		logger.info("Fetched ${exams.size} exams for ${untisSettings.username}")

		return exams
	}

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: Node, input: IntermediateData) {
		val jobContext: JobContext = input.get()
		val fetchContext = progress(jobContext, 1)

		iterableStructOutput(fetchContext) {
			fetchExams(node, logger)
		}
	}
}
