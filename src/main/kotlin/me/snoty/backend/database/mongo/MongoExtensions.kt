package me.snoty.backend.database.mongo

import me.snoty.integration.common.wiring.node.NodeDescriptor
import kotlin.reflect.KProperty

val NodeDescriptor.mongoCollectionPrefix: String
	get() = "$subsystem.$type"

val KProperty<*>.mongoField
	get() = "\$$name"
