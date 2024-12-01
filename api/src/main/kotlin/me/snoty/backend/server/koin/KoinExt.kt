@file:Suppress("PackageDirectoryMismatch") // no snoty-specific extensions, just extending on the koin-ktor library
package org.koin.ktor.ext

import io.ktor.server.routing.*
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

inline fun <reified T : Any> RoutingContext.get(
	qualifier: Qualifier? = null,
	noinline parameters: ParametersDefinition? = null,
) = call.get<T>(qualifier, parameters)
