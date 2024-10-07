package me.snoty.integration.untis.node.exam

import kotlinx.serialization.Serializable
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.mapWithSettings
import me.snoty.integration.common.wiring.iterableStructOutput
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.untis.WebUntisAPI
import me.snoty.integration.untis.WebUntisSettings
import me.snoty.integration.untis.model.UntisExam
import me.snoty.integration.untis.request.getExams
import org.koin.core.annotation.Single

@RegisterNode(
	displayName = "WebUntis Exams",
	type = "webuntis_exams",
	position = NodePosition.START,
	settingsType = WebUntisExamSettings::class,
	outputType = UntisExam::class,
)
@Single
class WebUntisExamNodeHandler(
	val metadata: NodeMetadata,
	private val untisAPI: WebUntisAPI
) : NodeHandler {
	override suspend fun NodeHandleContext.process(node: Node, input: Collection<IntermediateData>) = input.mapWithSettings<WebUntisSettings>(node) { settings ->
		val exams = untisAPI.getExams(settings)

		logger.info("Fetched ${exams.size} exams for ${settings.username}")

		iterableStructOutput(exams)
	}
}

@Serializable
data class WebUntisExamSettings(
	override val name: String = "WebUntis Exams",
	override val baseUrl: String,
	override val school: String,
	override val username: String,
	@FieldCensored
	override val appSecret: String,
) : WebUntisSettings