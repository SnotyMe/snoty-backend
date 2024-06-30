package me.snoty.backend.integration.flow.node

import me.snoty.backend.integration.flow.model.NodeDescriptor
import me.snoty.backend.integration.flow.model.Subsystem
import me.snoty.backend.test.NoOpNodeHandler
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
		registry.registerHandler(descriptor, NoOpNodeHandler)
		val node = registry.lookupHandler(descriptor)
		assertEquals(NoOpNodeHandler, node)
	}

	@Test
	fun testLookup_element_noMatch() {
		val descriptor = NodeDescriptor(Subsystem.INTEGRATION, "product")
		registry.registerHandler(descriptor, NoOpNodeHandler)
		var node = registry.lookupHandler(NodeDescriptor(Subsystem.INTEGRATION, "product2"))
		assertEquals(null, node)
		node = registry.lookupHandler(NodeDescriptor(Subsystem.PROCESSOR, "product2"))
		assertEquals(null, node)
	}

	@Test
	fun testLookup_subsystem() {
		val descriptor = NodeDescriptor(Subsystem.INTEGRATION, "product")
		registry.registerSubsystemHandler(Subsystem.INTEGRATION, NoOpNodeHandler)

		val node = registry.lookupHandler(descriptor)
		assertEquals(NoOpNodeHandler, node)
	}

	@Test
	fun testLookup_subsystem_noMatch() {
		registry.registerSubsystemHandler(Subsystem.INTEGRATION, NoOpNodeHandler)

		val node = registry.lookupHandler(NodeDescriptor(Subsystem.PROCESSOR, "product2"))

		assertEquals(null, node)
	}
}
