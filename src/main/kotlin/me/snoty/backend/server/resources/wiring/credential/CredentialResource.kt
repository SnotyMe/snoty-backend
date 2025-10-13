package me.snoty.backend.server.resources.wiring.credential

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.utils.BadRequestException
import me.snoty.backend.utils.NotFoundException
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.respondStatus
import me.snoty.backend.wiring.credential.CredentialService
import org.koin.ktor.ext.get

fun Route.credentialResource() {
	val credentialService: CredentialService = get()

	get("{id}") {
		val user = call.getUser()
		val credentialId = call.parameters["id"] ?: return@get call.respondStatus(BadRequestException("Missing credential ID"))

		val credential = credentialService.resolve(credentialId = credentialId, userId = user.id.toString())
			?: call.respondStatus(NotFoundException("Credential not found"))

		call.respond(credential)
	}

	route("{credentialType}") {
		get("enumerate") {
			val user = call.getUser()
			val credentialType = call.parameters["credentialType"] ?: return@get call.respondStatus(BadRequestException("Missing credential type"))

			val credentials = credentialService.enumerateCredentials(userId = user.id.toString(), credentialType = credentialType)

			call.respond(credentials)
		}
	}

	post {
		val user = call.getUser()
		val credentialCreateDto: CredentialCreateDto = call.receive()

		val created = credentialService.create(
			userId = user.id.toString(),
			name = credentialCreateDto.name,
			credentialType = credentialCreateDto.type,
			data = credentialCreateDto.data,
		)

		call.respondText(created)
	}
}
