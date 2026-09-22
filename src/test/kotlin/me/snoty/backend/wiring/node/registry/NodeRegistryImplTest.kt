package me.snoty.backend.wiring.node.registry

import me.snoty.backend.test.NoOpNodeHandler
import me.snoty.backend.test.nodeMetadata
import me.snoty.core.node.NodeType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class NodeRegistryImplTest {
	private val registry = NodeRegistryImpl()

	@Test
	fun testLookup_noElement() {
		val node = registry.lookupHandler(NodeType("product"))
		assertEquals(null, node)
	}

	@Test
	fun testLookup_element() {
		val type = NodeType("product")
		registry.registerHandler(nodeMetadata(type), NoOpNodeHandler)
		val node = registry.lookupHandler(type)
		assertEquals(NoOpNodeHandler, node)
	}

	@Test
	fun testLookup_element_noMatch() {
		val type = NodeType("product")
		registry.registerHandler(nodeMetadata(type), NoOpNodeHandler)
		var node = registry.lookupHandler(NodeType("product2"))
		assertEquals(null, node)
		node = registry.lookupHandler(NodeType("product2"))
		assertEquals(null, node)
	}
}
