/**
 * Ktor Koin extensions for Routing class
 *
 * @author Arnaud Giuliani
 * @author Laurent Baresse
 */

package me.snoty.backend.server.koin

import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.*
import me.snoty.backend.server.plugins.getKoin
import org.koin.core.Koin
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

/**
 * inject lazily given dependency
 * @param qualifier - bean name / optional
 * @param parameters
 */
inline fun <reified T : Any> Route.inject(
	qualifier: Qualifier? = null,
	noinline parameters: ParametersDefinition? = null
) =
	lazy { get<T>(qualifier, parameters) }

/**
 * Retrieve given dependency for KoinComponent
 * @param qualifier - bean name / optional
 * @param parameters
 */
inline fun <reified T : Any> Route.get(
	qualifier: Qualifier? = null,
	noinline parameters: ParametersDefinition? = null
) =
	application.getKoin().get<T>(qualifier, parameters)

/**
 * Retrieve given dependency for KoinComponent
 * @param qualifier - bean name / optional
 * @param parameters
 */
inline fun <reified T : Any> ApplicationCall.get(
	qualifier: Qualifier? = null,
	noinline parameters: ParametersDefinition? = null
) =
	application.getKoin().get<T>(qualifier, parameters)

inline fun <reified T : Any> RoutingContext.get(
	qualifier: Qualifier? = null,
	noinline parameters: ParametersDefinition? = null
) =
	call.get<T>(qualifier, parameters)

/**
 * Retrieve given property for KoinComponent
 * @param key - key property
 */
fun <T : Any> Route.getProperty(key: String) =
	application.getKoin().getProperty<T>(key)

/**
 * Retrieve given property for KoinComponent
 * give a default value if property is missing
 *
 * @param key - key property
 * @param defaultValue - default value if property is missing
 *
 */
inline fun <reified T> Route.getProperty(key: String, defaultValue: T) =
	application.getKoin().getProperty(key) ?: defaultValue

fun Routing.getKoin(): Koin = application.getKoin()
