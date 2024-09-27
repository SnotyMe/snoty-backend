package me.snoty.integration.builtin.calendar

import io.ktor.http.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import me.snoty.backend.utils.ForbiddenException
import me.snoty.backend.utils.respondStatus
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.FieldDescription
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.each
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.node.*
import net.fortuna.ical4j.data.CalendarOutputter
import org.koin.core.annotation.Single
import java.nio.charset.StandardCharsets

@Serializable
data class ICalSettings(
	override val name: String = "Calendar",
	@FieldDescription("A secret that has to be provided to access the calendar")
	val secret: String? = null,
) : NodeSettings

@RegisterNode(
	displayName = "ICal",
	type = "ical",
	subsystem = Subsystem.INTEGRATION,
	position = NodePosition.END,
	settingsType = ICalSettings::class,
	inputType = CalendarEvent::class,
)
@Single
class ICalNodeHandler(
	persistenceFactory: NodePersistenceFactory,
	nodeRouteFactory: NodeRouteFactory,
) : NodeHandler {
	private val eventPersistenceService = persistenceFactory<CalendarEvent>("events")

	init {
		nodeRouteFactory("calendar.ics", HttpMethod.Get, verifyUser = false) { node ->
			val secret = node.getConfig<ICalSettings>().secret
			if (!secret.isNullOrEmpty() && secret != call.queryParameters["secret"]) {
				call.respondStatus(ForbiddenException("Invalid calendar secret"))
				return@nodeRouteFactory
			}

			val events = eventPersistenceService.getEntities(node)

			val calendar = ICalBuilder.build(node.settings.name, events)

			val contentType = calendar.getContentType(StandardCharsets.UTF_8)
			val outputter = CalendarOutputter()
			call.respondOutputStream(ContentType.parse(contentType)) {
				outputter.output(calendar, this)
			}
		}
	}

	override suspend fun NodeHandleContext.process(node: Node, input: Collection<IntermediateData>) = each<CalendarEvent>(input) { data ->
		if (data.date == null && (data.startDate == null || data.endDate == null)) {
			logger.error("Event has no date or start/end date")
			return@each
		}

		eventPersistenceService.persistEntity(node, data.id, data)
	}
}
