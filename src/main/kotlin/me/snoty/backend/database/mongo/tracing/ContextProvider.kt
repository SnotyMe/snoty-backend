package me.snoty.backend.database.mongo.tracing

import com.mongodb.reactivestreams.client.ReactiveContextProvider
import io.opentelemetry.api.trace.Span
import org.reactivestreams.Subscriber

class ContextProvider : ReactiveContextProvider {
	override fun getContext(subscriber: Subscriber<*>) = RequestContextMap()
		.apply {
			val currentSpan = Span.current()
			if (currentSpan != Span.getInvalid()) {
				put(Span::class, currentSpan)
			}
		}
}
