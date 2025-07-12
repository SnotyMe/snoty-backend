package me.snoty.integration.common.wiring.data

import me.snoty.backend.test.TestCodecRegistry
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateDataMapper
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateDataMapper
import org.bson.Document
import org.bson.codecs.DocumentCodec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class IntermediateDataMapperRegistryImplTest {
	private val bson = BsonIntermediateDataMapper(TestCodecRegistry, TestCodecRegistry.get(Document::class.java) as DocumentCodec)
	private val simple = SimpleIntermediateDataMapper()

	@Test
	fun `getFirstCompatibleMapper should return the first mapper that supports the given type`() {
		fun asserts(registry: IntermediateDataMapperRegistry) {
			assertEquals(bson, registry.getFirstCompatibleMapper(Document::class))
			assertEquals(simple, registry.getFirstCompatibleMapper(String::class))
			assertEquals(simple, registry.getFirstCompatibleMapper(Double::class))
		}

		asserts(IntermediateDataMapperRegistryImpl(listOf(bson, simple)))
		asserts(IntermediateDataMapperRegistryImpl(listOf(simple, bson)))
	}

	@Test
	fun `getFirstSupportingMapper should throw an exception if no mapper supports the given type`() {
		val registry = IntermediateDataMapperRegistryImpl(listOf(bson))

		assertThrows(IllegalStateException::class.java) {
			registry.getFirstCompatibleMapper(Int::class)
		}.also {
			assertEquals("No mapper found for class kotlin.Int", it.message)
		}
	}

	@Test
	fun `get should return the correct mapper for the given type`() {
		fun asserts(registry: IntermediateDataMapperRegistry) {
			assertEquals(bson, registry[BsonIntermediateData::class])
			assertEquals(simple, registry[SimpleIntermediateData::class])
		}

		asserts(IntermediateDataMapperRegistryImpl(listOf(bson, simple)))
		asserts(IntermediateDataMapperRegistryImpl(listOf(simple, bson)))

		val bsonRegistry = IntermediateDataMapperRegistryImpl(listOf(bson))
		assertEquals(bson, bsonRegistry[BsonIntermediateData::class])
		assertThrows(IllegalStateException::class.java) {
			bsonRegistry[SimpleIntermediateData::class]
		}.also {
			assertEquals("No mapper found for ${SimpleIntermediateData::class}", it.message)
		}
	}
}
