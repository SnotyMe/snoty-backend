package me.snoty.integration.builtin.mapper

import org.bson.Document
import org.junit.jupiter.api.Test

class MapperNodeUtilsTest {
	@Test
	fun `test trimAll on Document`() {
		val document = Document(
			mapOf(
				"key1" to " value1 ",
				"key2" to listOf("\tvalue2a \n\tvalue3a", " value2b ", 1337),
				"key3" to Document(mapOf(
					"subKey" to " subValue ",
					"subList" to listOf("\tsubItem1 ", " subItem2 "),
					"subDocument" to Document(mapOf("deepKey" to " deepValue ")),
					"subEmpty" to "",
					"subNull" to null,
					"subNumber" to 1337
				)),
				"key4" to 1337
			)
		)

		val trimmedDocument = document.trimAll()

		assert(trimmedDocument["key1"] == "value1")
		assert(trimmedDocument["key2"] == listOf("value2a\nvalue3a", "value2b", 1337))
		assert(trimmedDocument["key3"] is Document)
		val subDocument = trimmedDocument["key3"] as Document
		assert(subDocument["subKey"] == "subValue")
		assert(subDocument["subList"] == listOf("subItem1", "subItem2"))
		assert(subDocument["subDocument"] is Document)
		val deepDocument = subDocument["subDocument"] as Document
		assert(deepDocument["deepKey"] == "deepValue")
		assert(subDocument["subEmpty"] == "")
		assert(subDocument["subNull"] == null)
		assert(subDocument["subNumber"] == 1337)
		assert(trimmedDocument["key4"] == 1337)
		assert(trimmedDocument.size == 4) // Ensure no extra keys were added
	}
}
