package me.snoty.integration.common.diff

import io.mockk.mockk
import kotlinx.datetime.LocalDateTime
import me.snoty.backend.database.mongo.apiCodecModule
import me.snoty.integration.common.utils.integrationsApiCodecModule
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChangeCodecTest {
	private val codecs: CodecRegistry = CodecRegistries.fromRegistries(
		integrationsApiCodecModule(),
		apiCodecModule()
	)

	private fun <T : Any> serializeChange(change: Change<T>): BsonDocument {
		val document = BsonDocument()
		val changeCodec = codecs.get(Change::class.java)
		changeCodec.encode(
			BsonDocumentWriter(document),
			change,
			mockk()
		)
		return document
	}

	private fun deserializeChange(document: BsonDocument): Change<*> {
		val changeCodec = codecs.get(Change::class.java)
		return changeCodec.decode(
			BsonDocumentReader(document),
			mockk()
		)!!
	}

	@Test
	fun `test encode LocalDateTime`() {
		val old = LocalDateTime.parse("2020-12-31T23:59:59")
		val new = LocalDateTime.parse("2021-01-01T00:00:00")
		val change = Change(LocalDateTime::class, old, new)

		val document = serializeChange(change)
		assertEquals(3, document.size)
		assertEquals("kotlinx.datetime.LocalDateTime", document["type"]?.asString()?.value)
		assertEquals(old, LocalDateTime.parse(document.getString("old").value))

		val parsedChange = deserializeChange(document)
		assertEquals(change, parsedChange)
	}

	@Test
	fun `test encode String`() {
		val old = "old"
		val new = "new"
		val change = Change(String::class, old, new)

		val document = serializeChange(change)
		assertEquals(3, document.size)
		assertEquals("java.lang.String", document["type"]?.asString()?.value)
		assertEquals(old, document.getString("old").value)

		val parsedChange = deserializeChange(document)
		assertEquals(change, parsedChange)
	}

	@Test
	fun `test encode Int`() {
		val old = 1
		val new = 2
		val change = Change(Int::class, old, new)

		val document = serializeChange(change)
		assertEquals(3, document.size)
		assertEquals("java.lang.Integer", document["type"]?.asString()?.value)
		assertEquals(old, document.getInt32("old").value)

		val parsedChange = deserializeChange(document)
		assertEquals(change, parsedChange)
	}

	@Test
	fun `test encode Long`() {
		val old = 1L
		val new = 2L
		val change = Change(Long::class, old, new)

		val document = serializeChange(change)
		assertEquals(3, document.size)
		assertEquals("java.lang.Long", document["type"]?.asString()?.value)
		assertEquals(old, document.getInt64("old").value)

		val parsedChange = deserializeChange(document)
		assertEquals(change, parsedChange)
	}

	@Test
	fun `test encode Double`() {
		val old = 1.0
		val new = 2.0
		val change = Change(Double::class, old, new)

		val document = serializeChange(change)
		assertEquals(3, document.size)
		assertEquals("java.lang.Double", document["type"]?.asString()?.value)
		assertEquals(old, document.getDouble("old").value)

		val parsedChange = deserializeChange(document)
		assertEquals(change, parsedChange)
	}
}
