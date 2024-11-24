package me.snoty.backend.server.resources.wiring.flow

import io.ktor.http.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.server.koin.get
import me.snoty.backend.utils.getUser
import me.snoty.backend.wiring.flow.export.FlowExportService
import me.snoty.backend.wiring.flow.import.FlowImportService
import me.snoty.backend.wiring.flow.import.ImportFlow

fun Route.flowExportImportResource() {
	val exportService: FlowExportService = get()
	val importService: FlowImportService = get()

	route("{id}") {
		get("export") {
			val flow = getPersonalFlowOrNull() ?: return@get

			val exported = exportService.export(flow._id)

			call.response.header(
				HttpHeaders.ContentDisposition,
				ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "${flow.name}.json")
					.toString()
			)
			call.respond(exported)
		}
	}

	post("import") {
		val user = call.getUser()

		val imported: ImportFlow = call.receive()
		val createdId = importService.import(user.id, imported)

		call.respondText(text = createdId.toString(), status = HttpStatusCode.OK)
	}
}
