package me.snoty.backend.wiring.node.metadata

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.snoty.backend.metadata.Icon
import me.snoty.backend.schema.ObjectSchema
import me.snoty.backend.wiring.node.NodeSettings
import me.snoty.core.node.NodeType
import kotlin.reflect.KClass

@Serializable
data class NodeMetadata(
	val displayName: String,
	val icon: Icon? = null,
	val type: NodeType,
	val stereotype: NodeStereotype,
	val settings: ObjectSchema,
	@Transient
	val settingsClass: KClass<out NodeSettings> = NodeSettings::class, // previously threw an error, but since we deserialize this AND remain compatible, we need to keep this
	val input: ObjectSchema?,
	val receiveEmptyInput: Boolean = false,
	val output: ObjectSchema?
)

enum class NodeStereotype(
	val logOutput: Boolean,
) {
	START(
		logOutput = true
	),
	MIDDLE(
		logOutput = true
	),
	END(
		logOutput = false
	)
}
