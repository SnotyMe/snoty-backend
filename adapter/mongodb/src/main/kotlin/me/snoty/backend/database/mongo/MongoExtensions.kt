package me.snoty.backend.database.mongo

import me.snoty.core.node.NodeType

val NodeType.mongoCollectionPrefix: String
	get() = "nodes:$value"
