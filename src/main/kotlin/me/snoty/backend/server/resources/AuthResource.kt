package me.snoty.backend.server.resources

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import me.snoty.backend.authentication.AuthenticationMetadata
import me.snoty.backend.injection.getFromAllScopes
import me.snoty.backend.server.routing.Resource
import org.koin.core.Koin
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
@Named("auth")
fun AuthResource(koin: Koin) = Resource {
    route("auth") {
        get("metadata") {
            val metadata: AuthenticationMetadata = koin.get()
            call.respond(metadata)
        }.describe {
            responses {
                HttpStatusCode.OK {
                    schema = JsonSchema(
                        title = AuthenticationMetadata::class.simpleName!!,
                        oneOf = koin.getFromAllScopes<JsonSchema>()
                            .filter {
                                it.discriminator?.propertyName == AuthenticationMetadata::class.simpleName
                            }
                            .map { ReferenceOr.Value(it) }
                    )
                }
            }
        }
    }
}
