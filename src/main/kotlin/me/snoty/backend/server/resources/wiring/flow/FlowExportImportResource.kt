package me.snoty.backend.server.resources.wiring.flow

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.snoty.backend.utils.getUser
import me.snoty.backend.wiring.flow.export.FlowExportService
import me.snoty.backend.wiring.flow.import.FlowImportService
import me.snoty.backend.wiring.flow.import.ImportFlow
import org.koin.ktor.ext.get

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
		
		post("export") {
			@Serializable
			data class ExportOptions(
				val withSensitiveData: Boolean = false,
			)

			val options: ExportOptions? = call.receiveNullable()
			val flow = getPersonalFlowOrNull() ?: return@post

			val censor = options?.withSensitiveData?.let { !it } ?: true
			val exported = exportService.export(flow._id, censor = censor)
			
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
