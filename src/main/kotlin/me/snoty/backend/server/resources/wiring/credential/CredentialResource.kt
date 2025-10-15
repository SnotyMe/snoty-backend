package me.snoty.backend.server.resources.wiring.credential

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import me.snoty.backend.utils.BadRequestException
import me.snoty.backend.utils.NotFoundException
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.respondStatus
import me.snoty.backend.wiring.credential.CredentialDefinitionRegistry
import me.snoty.backend.wiring.credential.CredentialService
import org.koin.ktor.ext.get as getDependency

@OptIn(InternalSerializationApi::class)
fun Route.credentialResource() {
	val credentialDefinitionRegistry: CredentialDefinitionRegistry = getDependency()
	val credentialService: CredentialService = getDependency()
	val json: Json = getDependency()

	post {
		val user = call.getUser()
		val credentialCreateDto: CredentialCreateDto = call.receive()

		val created = credentialService.create(
			userId = user.id.toString(),
			name = credentialCreateDto.name,
			credentialType = credentialCreateDto.type,
			data = credentialCreateDto.data.convertToCredential(json, credentialDefinitionRegistry, credentialCreateDto.type),
		)

		call.respond(HttpStatusCode.Created, created)
	}

	route("{id}") {
		fun RoutingContext.parseCredentialId() = call.parameters["id"] ?: throw BadRequestException("Missing credential ID")

		get {
			val credentialId = parseCredentialId()
			val user = call.getUser()

			val credential = credentialService.resolve(userId = user.id.toString(), credentialId = credentialId)
				?: call.respondStatus(NotFoundException("Credential not found"))

			call.respond(credential)
		}

		put {
			val credentialId = parseCredentialId()
			val user = call.getUser()
			val credentialUpdateDto: CredentialUpdateDto = call.receive()

			val existing = credentialService.resolve(userId = user.id.toString(), credentialId = credentialId)
				?: return@put call.respondStatus(NotFoundException("Credential not found"))

			TODO()
/*
			val updated = credentialService.update(
				userId = user.id.toString(),
				credentialId = credentialId,
				name = credentialUpdateDto.name,
				data = credentialUpdateDto.data.convertToCredential(json, credentialDefinitionRegistry, existing.type),
			) ?: call.respondStatus(NotFoundException("Credential not found"))
*/
		}
	}

	get("overview") {
		val user = call.getUser()

		val credentials = credentialService.listDefinitionsWithStatistics(userId = user.id.toString())

		call.respond(credentials)
	}

	route("{credentialType}") {
		get("enumerate") {
			val user = call.getUser()
			val credentialType = call.parameters["credentialType"] ?: return@get call.respondStatus(BadRequestException("Missing credential type"))

			val credentials = credentialService.enumerateCredentials(userId = user.id.toString(), credentialType = credentialType)

			call.respond(credentials)
		}

		get("list") {
			val user = call.getUser()
			val credentialType = call.parameters["credentialType"] ?: return@get call.respondStatus(BadRequestException("Missing credential type"))

			val credentials = credentialService.listCredentials(userId = user.id.toString(), credentialType = credentialType)

			call.respond(credentials)
		}
	}
}
