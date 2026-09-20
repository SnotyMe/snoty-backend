package me.snoty.node.untis.node.timetable

import kotlinx.serialization.Serializable
import me.snoty.backend.schema.FieldCensored
import me.snoty.backend.wiring.data.IntermediateData
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
import me.snoty.node.untis.request.getTimetable
import org.koin.core.annotation.Single

@RegisterNode(
	name = "webuntis_timetable",
	displayName = "WebUntis Timetable",
	icon = Icon(name = "arcticons-untis-mobile", color = "#FF6033"),
	stereotype = NodeStereotype.START,
	settingsType = WebUntisTimetableSettings::class,
	outputType = MappedUntisPeriod::class,
)
@Single
class WebUntisTimetableNodeHandler(
	val metadata: NodeMetadata,
	private val untisAPI: WebUntisAPI
) : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(node: NodeWithSettings, input: Collection<IntermediateData>): NodeOutput {
		val settings: WebUntisTimetableSettings = node.getConfig()

		val (timetable, masterData) = untisAPI.getTimetable(settings)
		val mappedMasterData = masterData.map()

		logger.info("Fetched ${timetable.periods.size} periods for ${settings.username}")

		val periods = timetable.periods
			.map { it.toUntisPeriod(mappedMasterData) }

		return iterableStructOutput(periods)
	}
}

@Serializable
data class WebUntisTimetableSettings(
	override val baseUrl: String,
	override val school: String,
	override val username: String,
	@FieldCensored
	override val appSecret: String,
) : WebUntisSettings
