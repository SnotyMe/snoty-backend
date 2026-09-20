package me.snoty.backend.metadata

import kotlinx.serialization.Serializable
import me.snoty.backend.wiring.node.Icon as IconAnnotation

@Serializable
data class Icon(
	val name: String,
	val color: String? = null,
) {
	companion object {
		fun of(icon: IconAnnotation) = Icon(
			name = icon.name,
			color = icon.color.takeIf(String::isNotEmpty)
		)
	}
}
