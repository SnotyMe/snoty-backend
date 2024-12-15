package me.snoty.backend.integration.flow.node

import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.backend.test.NoOpNodeHandler
import me.snoty.backend.test.nodeMetadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class NodeRegistryImplTest {
	private val registry = NodeRegistryImpl()
	private val namespace = javaClass.packageName

	@Test
	fun testLookup_noElement() {
		val node = registry.lookupHandler(NodeDescriptor(namespace, "product"))
		assertEquals(null, node)
	}

	@Test
	fun testLookup_element() {
		val descriptor = NodeDescriptor(namespace, "product")
		registry.registerHandler(nodeMetadata(descriptor), NoOpNodeHandler)
		val node = registry.lookupHandler(descriptor)
		assertEquals(NoOpNodeHandler, node)
	}

	@Test
	fun testLookup_element_noMatch() {
		val descriptor = NodeDescriptor(namespace, "product")
		registry.registerHandler(nodeMetadata(descriptor), NoOpNodeHandler)
		var node = registry.lookupHandler(NodeDescriptor(namespace, "product2"))
		assertEquals(null, node)
		node = registry.lookupHandler(NodeDescriptor(namespace, "product2"))
		assertEquals(null, node)
	}
}
