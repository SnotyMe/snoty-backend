package me.snoty.integration.untis.node.timetable

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
import me.snoty.integration.untis.model.map
import me.snoty.integration.untis.model.timetable.UntisPeriod
import me.snoty.integration.untis.request.getTimetable
import org.koin.core.annotation.Single

@RegisterNode(
	displayName = "WebUntis Timetable",
	type = "webuntis_timetable",
	position = NodePosition.START,
	settingsType = WebUntisTimetableSettings::class,
	outputType = MappedUntisPeriod::class,
)
@Single
class WebUntisTimetableNodeHandler(
	val metadata: NodeMetadata,
	private val untisAPI: WebUntisAPI
) : NodeHandler {
	override suspend fun NodeHandleContext.process(node: Node, input: Collection<IntermediateData>) = input.mapWithSettings<WebUntisSettings>(node) { settings ->
		val (timetable, masterData) = untisAPI.getTimetable(settings)
		val mappedMasterData = masterData.map()

		logger.info("Fetched ${timetable.periods.size} periods for ${settings.username}")

		val periods = timetable.periods
			.map { it.toUntisPeriod(mappedMasterData) }

		iterableStructOutput(periods)
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
