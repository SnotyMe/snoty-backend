package me.snoty.backend.server.resources

import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.authentication.AuthenticationMetadata
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
        }
    }
}
