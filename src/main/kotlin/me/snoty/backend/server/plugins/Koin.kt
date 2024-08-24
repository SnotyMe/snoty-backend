package me.snoty.backend.server.plugins

import io.ktor.server.application.*
import io.ktor.util.*
import org.koin.core.Koin
import org.koin.core.scope.Scope

const val KOIN_KEY = "KOIN"
val KOIN_ATTRIBUTE_KEY = AttributeKey<Koin>(KOIN_KEY)

const val KOIN_SCOPE_KEY = "KOIN_SCOPE"
val KOIN_SCOPE_ATTRIBUTE_KEY = AttributeKey<Scope>(KOIN_SCOPE_KEY)

val ApplicationCall.scope: Scope
	get() = this.attributes.getOrNull(KOIN_SCOPE_ATTRIBUTE_KEY)
		?: error("Koin Request Scope is not ready")

fun Application.setKoin(koin: Koin) {
	attributes.put(KOIN_ATTRIBUTE_KEY, koin)
}

fun Application.getKoin(): Koin = attributes[KOIN_ATTRIBUTE_KEY]
