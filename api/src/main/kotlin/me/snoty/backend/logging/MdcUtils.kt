package me.snoty.backend.logging

import io.opentelemetry.api.common.AttributeKey
import org.slf4j.MDC

object KMDC {
	fun put(key: AttributeKey<String>, value: String) {
		MDC.put(key.key, value)
	}
}
