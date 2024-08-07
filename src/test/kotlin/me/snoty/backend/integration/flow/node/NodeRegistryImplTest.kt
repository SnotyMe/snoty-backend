package me.snoty.backend.integration.flow.node

import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.Subsystem
import me.snoty.backend.test.NoOpNodeHandler
import me.snoty.backend.test.TestNodeMetadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class NodeRegistryImplTest {
	private val registry = NodeRegistryImpl()

	@Test
	fun testLookup_noElement() {
		val node = registry.lookupHandler(NodeDescriptor(Subsystem.INTEGRATION, "product"))
		assertEquals(null, node)
	}

	@Test
	fun testLookup_element() {
		val descriptor = NodeDescriptor(Subsystem.INTEGRATION, "product")
		registry.registerHandler(descriptor, TestNodeMetadata, NoOpNodeHandler)
		val node = registry.lookupHandler(descriptor)
		assertEquals(NoOpNodeHandler, node)
	}

	@Test
	fun testLookup_element_noMatch() {
		val descriptor = NodeDescriptor(Subsystem.INTEGRATION, "product")
		registry.registerHandler(descriptor, TestNodeMetadata, NoOpNodeHandler)
		var node = registry.lookupHandler(NodeDescriptor(Subsystem.INTEGRATION, "product2"))
		assertEquals(null, node)
		node = registry.lookupHandler(NodeDescriptor(Subsystem.PROCESSOR, "product2"))
		assertEquals(null, node)
	}
}
