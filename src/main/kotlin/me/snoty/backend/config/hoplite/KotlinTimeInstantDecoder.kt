package me.snoty.backend.config.hoplite

import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.decoder.NonNullableLeafDecoder
import com.sksamuel.hoplite.decoder.toValidated
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KType
import kotlin.time.Instant

/**
 * https://github.com/sksamuel/hoplite/issues/501
 * TODO: remove this when Hoplite supports Kotlin Time natively
 */
class KotlinTimeInstantDecoder : NonNullableLeafDecoder<Instant> {
	override fun supports(type: KType): Boolean = type.classifier == Instant::class
	override fun safeLeafDecode(
		node: Node,
		type: KType,
		context: DecoderContext
	): ConfigResult<Instant> = when (node) {
		is StringNode -> runCatching { Instant.fromEpochMilliseconds(node.value.toLong()) }.recoverCatching { Instant.parse(node.value) }.toValidated {
			ConfigFailure.DecodeError(node, type)
		}

		is LongNode -> Instant.fromEpochMilliseconds(node.value).valid()
		else -> ConfigFailure.DecodeError(node, type).invalid()
	}
}
