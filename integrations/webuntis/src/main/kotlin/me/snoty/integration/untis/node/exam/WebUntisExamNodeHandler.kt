package me.snoty.integration.untis.node.exam

import kotlinx.serialization.Serializable
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.NodeInput
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.iterableStructOutput
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.untis.WebUntisAPI
import me.snoty.integration.untis.WebUntisSettings
import me.snoty.integration.untis.model.map
import me.snoty.integration.untis.request.getExams
import me.snoty.integration.untis.request.getUserAndMasterData
import org.koin.core.annotation.Single

@RegisterNode(
	name = "webuntis_exams",
	displayName = "WebUntis Exams",
	position = NodePosition.START,
	settingsType = WebUntisExamSettings::class,
	outputType = MappedUntisExam::class,
)
@Single
class WebUntisExamNodeHandler(
	val metadata: NodeMetadata,
	private val untisAPI: WebUntisAPI
) : NodeHandler {
	override suspend fun NodeHandleContext.process(node: Node, input: NodeInput): NodeOutput {
		val settings: WebUntisExamSettings = node.getConfig()

		val (userData, masterData) = untisAPI.getUserAndMasterData(settings)
		val untisExams = untisAPI.getExams(settings, userData)

		val mappedMasterData = masterData.map()
		val mappedExams = untisExams
			.map { it.map(mappedMasterData) }

		logger.info("Fetched ${mappedExams.size} exams for ${settings.username}")

		return iterableStructOutput(mappedExams)
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
