package me.snoty.integration.untis.node.timetable

import kotlinx.serialization.Serializable
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.data.iterableStructOutput
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.logger
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.untis.WebUntisAPI
import me.snoty.integration.untis.WebUntisSettings
import me.snoty.integration.untis.model.map
import me.snoty.integration.untis.request.getTimetable
import org.koin.core.annotation.Single

@RegisterNode(
	name = "webuntis_timetable",
	displayName = "WebUntis Timetable",
	position = NodePosition.START,
	settingsType = WebUntisTimetableSettings::class,
	outputType = MappedUntisPeriod::class,
)
@Single
class WebUntisTimetableNodeHandler(
	val metadata: NodeMetadata,
	private val untisAPI: WebUntisAPI
) : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(node: Node, input: Collection<IntermediateData>): NodeOutput {
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
	override val name: String = "WebUntis Timetable",
	override val baseUrl: String,
	override val school: String,
	override val username: String,
	@FieldCensored
	override val appSecret: String,
) : WebUntisSettings
