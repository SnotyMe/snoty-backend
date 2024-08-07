package me.snoty.integration.common.annotation

import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.NoSchema
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.node.Subsystem
import kotlin.reflect.KClass

annotation class RegisterNode(
	val displayName: String,
	/**
	 * Unique node type / id, in snake_case.
	 */
	val type: String,
	val subsystem: String = Subsystem.INTEGRATION,
	val position: NodePosition,
	val settingsType: KClass<out NodeSettings>,
	val inputType: KClass<out Any> = NoSchema::class,
	val outputType: KClass<out Any> = NoSchema::class,
)
