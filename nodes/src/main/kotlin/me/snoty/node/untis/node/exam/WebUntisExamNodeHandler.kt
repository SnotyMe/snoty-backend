package me.snoty.node.untis.node.exam

import kotlinx.serialization.Serializable
import me.snoty.backend.schema.FieldCensored
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.data.iterableStructOutput
import me.snoty.backend.wiring.node.*
import me.snoty.backend.wiring.node.metadata.NodeMetadata
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeWithSettings
import me.snoty.core.node.getConfig
import me.snoty.node.untis.WebUntisAPI
import me.snoty.node.untis.WebUntisSettings
import me.snoty.node.untis.model.map
import me.snoty.node.untis.request.getExams
import me.snoty.node.untis.request.getUserAndMasterData
import org.koin.core.annotation.Single

@RegisterNode(
	name = "webuntis_exams",
	displayName = "WebUntis Exams",
	icon = Icon(name = "arcticons-untis-mobile", color = "#FF6033"),
	stereotype = NodeStereotype.START,
	settingsType = WebUntisExamSettings::class,
	outputType = MappedUntisExam::class,
)
@Single
class WebUntisExamNodeHandler(
	val metadata: NodeMetadata,
	private val untisAPI: WebUntisAPI
) : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(node: NodeWithSettings, input: NodeInput): NodeOutput {
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
	override val baseUrl: String,
	override val school: String,
	override val username: String,
	@FieldCensored
	override val appSecret: String,
) : WebUntisSettings
