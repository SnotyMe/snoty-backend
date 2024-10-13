package me.snoty.backend.database.mongo.tracing

import ch.qos.logback.core.spi.FilterReply
import com.mongodb.MongoSocketException
import com.mongodb.RequestContext
import com.mongodb.event.CommandFailedEvent
import com.mongodb.event.CommandListener
import com.mongodb.event.CommandStartedEvent
import com.mongodb.event.CommandSucceededEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.semconv.ServerAttributes
import me.snoty.backend.database.mongo.tracing.filter.MongoTracingFilter
import me.snoty.backend.observability.subspan
import org.bson.BsonValue
import org.koin.core.annotation.Single

@Single
class MongoTracing(openTelemetry: OpenTelemetry, private val featureFlags: MongoTracingFeatureFlags, private val filters: List<MongoTracingFilter>) : CommandListener {
	private val tracer = openTelemetry.getTracer("mongo")

	override fun commandStarted(event: CommandStartedEvent) {
		if (filters.any { it.decide(event) == FilterReply.DENY }) {
			logger.trace { "Filter denied the command" }
			return
		}

		logger.trace { "Instrumenting the command started event" }

		val databaseName = event.databaseName
		val requestContext = event.requestContext ?: return
		val parent = spanFromContext(requestContext)
		logger.trace { "Found the following span passed from the mongo context [$parent]" }
		val commandName = event.commandName
		val command = event.command
		val collectionName = getCollectionName(command, commandName)
		val spanName = getSpanName(commandName, collectionName)

		fun SpanBuilder.configure() {
			setAttribute(DATABASE_NAME, databaseName)
			collectionName?.let { setAttribute(COLLECTION_NAME, it) }
			setAttribute(OPERATION_NAME, commandName)
			if (featureFlags.traceQueries) {
				setAttribute(QUERY_TEXT, command.toJson())
			}

			val connectionDescription = event.connectionDescription
			try {
				val serverAddress = connectionDescription.serverAddress
				setAttribute(ServerAttributes.SERVER_PORT, serverAddress.port.toLong())
				setAttribute(ServerAttributes.SERVER_ADDRESS, serverAddress.host)
			} catch (ignored: MongoSocketException) {
				logger.trace(ignored) { "Ignored exception when setting remote ip and port" }
			}
		}

		val childSpan = parent?.subspan(tracer, spanName, SpanBuilder::configure)
			?: tracer.spanBuilder(spanName).apply(SpanBuilder::configure).startSpan()

		requestContext.put<Span>(childSpan)
		logger.trace {
			"Created a child span [$childSpan]"
		}
	}

	override fun commandSucceeded(event: CommandSucceededEvent) {
		val requestContext = event.requestContext ?: return
		val span = requestContext.get<Span>() ?: return
		logger.trace { "Command succeeded - will close span [$span]" }
		span.end()
		requestContext.delete<Span>()
	}

	override fun commandFailed(event: CommandFailedEvent) {
		val requestContext = event.requestContext ?: return
		val span = requestContext.get<Span>() ?: return
		logger.trace { "Command failed - will close span [$span]" }
		span.recordException(event.throwable)
		span.end()
		requestContext.delete<Span>()
	}

	companion object {
		private val logger = KotlinLogging.logger {}

		// See https://docs.mongodb.com/manual/reference/command for the command reference
		val COMMANDS_WITH_COLLECTION_NAME: Set<String> = LinkedHashSet(
			mutableListOf(
				"aggregate", "count", "distinct", "mapReduce", "geoSearch", "delete", "find", "findAndModify",
				"insert", "update", "collMod", "compact", "convertToCapped", "create", "createIndexes", "drop",
				"dropIndexes", "killCursors", "listIndexes", "reIndex"
			)
		)

		private fun spanFromContext(
			context: RequestContext
		): Span? {
			val span = context.get<Span>()
			if (span != null) {
				logger.trace { "Found a span in mongo context [$span]" }
				return span
			}
			logger.trace { "No span was found - will not create any child spans" }
			return null
		}

		/**
		 * @return trimmed string from `bsonValue` or null if the trimmed string was
		 * empty or the value wasn't a string
		 */
		fun getNonEmptyBsonString(bsonValue: BsonValue?): String? {
			if (bsonValue == null || !bsonValue.isString) {
				return null
			}
			val stringValue = bsonValue.asString().value.trim { it <= ' ' }
			return stringValue.ifEmpty { null }
		}

		fun getSpanName(commandName: String, collectionName: String?): String = when {
			collectionName == null -> commandName
			else -> "$commandName $collectionName"
		}
	}
}
