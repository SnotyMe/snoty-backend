package me.snoty.integration.builtin.calendar

import io.ktor.http.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import me.snoty.backend.utils.ForbiddenException
import me.snoty.backend.utils.respondStatus
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.FieldDescription
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.get
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.node.*
import net.fortuna.ical4j.data.CalendarOutputter
import org.koin.core.annotation.Single
import org.slf4j.Logger
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
	override val metadata: NodeMetadata,
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

	context(NodeHandleContext)
	override suspend fun process(logger: Logger, node: Node, input: IntermediateData) {
		val data: CalendarEvent = input.get()

		eventPersistenceService.persistEntity(node, data.id, data)
	}
}
