package me.snoty.integration.common.annotation

import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.NoSchema
import me.snoty.integration.common.wiring.node.NodeSettings
import kotlin.reflect.KClass

annotation class RegisterNode(
	val displayName: String,
	/**
	 * Namespace unique node name / id, in snake_case.
	 * Must only exist once within your namespace or side effects will occur.
	 */
	val name: String,
	/**
	 * Namespace of the node. Usually the package name of the node handler.
	 * Set to empty string to infer from the handler class.
	 *
	 * Useful when migrating nodes to a new package.
	 */
	val namespace: String = "",
	val position: NodePosition,
	val settingsType: KClass<out NodeSettings>,
	val inputType: KClass<out Any> = NoSchema::class,
	val outputType: KClass<out Any> = NoSchema::class,
)

/**
 * Marks that the node wants to receive empty input. Otherwise, following nodes will not be executed at all if no input element was produced by the prior node.
 */
annotation class ReceiveEmptyInput