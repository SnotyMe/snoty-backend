package me.snoty.backend.server.resources

import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import me.snoty.backend.authentication.AuthenticationAdapter
import me.snoty.backend.server.routing.Resource
import org.koin.core.Koin
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
@Named("auth")
fun AuthResource(koin: Koin, authenticationAdapter: AuthenticationAdapter) = Resource {
    val authenticationMetadata = CoroutineScope(Dispatchers.IO).async {
        authenticationAdapter.buildAuthenticationMetadata(koin)
    }

    route("auth") {
        get("metadata") {
            val metadata = authenticationMetadata.await()
            call.respond(metadata)
        }
    }
}
