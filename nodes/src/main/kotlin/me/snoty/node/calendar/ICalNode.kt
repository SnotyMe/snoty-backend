package me.snoty.node.calendar

import io.ktor.http.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import me.snoty.backend.schema.FieldCensored
import me.snoty.backend.schema.FieldDescription
import me.snoty.backend.utils.ForbiddenException
import me.snoty.backend.utils.filterNot
import me.snoty.backend.utils.respondStatus
import me.snoty.backend.wiring.data.IntermediateData
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.data.get
import me.snoty.backend.wiring.node.*
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.backend.wiring.node.persistence.NodePersistenceFactory
import me.snoty.backend.wiring.node.persistence.invoke
import me.snoty.backend.wiring.node.routing.NodeRouteFactory
import me.snoty.core.node.NodeWithSettings
import me.snoty.core.node.getConfig
import net.fortuna.ical4j.data.CalendarOutputter
import org.koin.core.annotation.Single
import java.nio.charset.StandardCharsets

@Serializable
data class ICalSettings(
	@FieldDescription("A secret that has to be provided to access the calendar")
	@FieldCensored
	val secret: String? = null,
) : NodeSettings

@RegisterNode(
	name = "ical",
	displayName = "ICal",
	icon = Icon(name = "lucide-calendar-days"),
	stereotype = NodeStereotype.END,
	settingsType = ICalSettings::class,
	inputType = CalendarEvent::class,
)
@Single
class ICalNodeHandler(
	persistenceFactory: NodePersistenceFactory,
	nodeRouteFactory: NodeRouteFactory,
	iCalBuilder: ICalBuilder,
) : NodeHandler {
	private val eventPersistenceService = persistenceFactory<CalendarEvent>("events")

	init {
		nodeRouteFactory("calendar.ics", HttpMethod.Get, verifyUser = false) { node ->
			val settings = node.getConfig<ICalSettings>()
			val secret = settings.secret
			if (!secret.isNullOrEmpty() && secret != call.queryParameters["secret"]) {
				call.respondStatus(ForbiddenException("Invalid calendar secret"))
				return@nodeRouteFactory
			}

			val events = eventPersistenceService.getEntities(node)

			val calendar = iCalBuilder.build(node.id.value, node.name, events)

			val contentType = calendar.getContentType(StandardCharsets.UTF_8)
			val outputter = CalendarOutputter()
			call.respondOutputStream(ContentType.parse(contentType)) {
				outputter.output(calendar, this)
			}
		}
	}

	context(_: NodeHandleContext)
	override suspend fun process(node: NodeWithSettings, input: Collection<IntermediateData>): NodeOutput {
		val events = input
			.map { it.get<CalendarEvent>() }
			.filterNot(
				predicate = { it.date == null && (it.startDate == null || it.endDate == null) },
				ifTrue = { logger.error("Event has no date or start/end date") }
			)
		eventPersistenceService.setEntities(node, events) { it.id }

		return emptyList()
	}
}
