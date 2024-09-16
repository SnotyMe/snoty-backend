package me.snoty.integration.untis

import io.ktor.client.*
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.*
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.mapWithSettings
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.untis.model.UntisExam
import me.snoty.integration.untis.request.getExams
import org.koin.core.annotation.Single
import org.slf4j.Logger

@RegisterNode(
	displayName = "WebUntis",
	type = "webuntis",
	position = NodePosition.START,
	settingsType = WebUntisSettings::class,
	outputType = UntisExam::class,
)
@Single
class WebUntisIntegration(
	val metadata: NodeMetadata,
	private val httpClient: HttpClient,
	private val untisAPI: WebUntisAPI = WebUntisAPIImpl(httpClient)
) : NodeHandler {
	context(NodeHandleContext)
	override suspend fun process(logger: Logger, node: Node, input: Collection<IntermediateData>) = input.mapWithSettings<WebUntisSettings>(node) { settings ->
		val exams = untisAPI.getExams(settings)

		logger.info("Fetched ${exams.size} exams for ${settings.username}")

		iterableStructOutput(exams)
	}
}
