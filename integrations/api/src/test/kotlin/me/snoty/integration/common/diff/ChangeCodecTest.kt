package me.snoty.integration.common.diff

import io.mockk.mockk
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.snoty.backend.database.mongo.apiCodecModule
import me.snoty.backend.database.mongo.decode
import me.snoty.integration.common.utils.bsonTypeClassMap
import me.snoty.integration.common.utils.integrationsApiCodecModule
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ChangeCodecTest {
	private val codecs: CodecRegistry = CodecRegistries.fromRegistries(
		integrationsApiCodecModule(bsonTypeClassMap()),
		apiCodecModule()
	)

	private fun <T1 : Any, T2: Any> serializeChange(change: Change<T1, T2>): BsonDocument {
		val document = BsonDocument()
		val changeCodec = codecs.get(Change::class.java)
		changeCodec.encode(
			BsonDocumentWriter(document),
			change,
			mockk()
		)
		return document
	}

	private fun deserializeChange(document: BsonDocument): Change<*, *> {
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
		val change = Change(old, new)

		val document = serializeChange(change)
		assertEquals(2, document.size)
		document.getDateTime("old")
		assertEquals(old, document.getDateTime("old").decode())
		assertEquals(new, document.getDateTime("new").decode())

		val parsedChange = deserializeChange(document)

		// LocalDateTime is mapped to Instant
		assertNotEquals(change, parsedChange)
		val oldInstant = old.toInstant(TimeZone.UTC)
		val newInstance = new.toInstant(TimeZone.UTC)
		assertEquals(old, change.old)
		assertEquals(oldInstant, parsedChange.old)
		assertEquals(new, change.new)
		assertEquals(newInstance, parsedChange.new)
	}

	@Test
	fun `test encode String`() {
		val old = "old"
		val new = "new"
		val change = Change( old, new)

		val document = serializeChange(change)
		assertEquals(2, document.size)
		assertEquals(old, document.getString("old").value)
		assertEquals(new, document.getString("new").value)

		val parsedChange = deserializeChange(document)
		assertEquals(change, parsedChange)
	}

	@Test
	fun `test encode Int`() {
		val old = 1
		val new = 2
		val change = Change(old, new)

		val document = serializeChange(change)
		assertEquals(2, document.size)
		assertEquals(old, document.getInt32("old").value)
		assertEquals(new, document.getInt32("new").value)

		val parsedChange = deserializeChange(document)
		assertEquals(change, parsedChange)
	}

	@Test
	fun `test encode Long`() {
		val old = 1L
		val new = 2L
		val change = Change(old, new)

		val document = serializeChange(change)
		assertEquals(2, document.size)
		assertEquals(old, document.getInt64("old").value)
		assertEquals(new, document.getInt64("new").value)

		val parsedChange = deserializeChange(document)
		assertEquals(change, parsedChange)
	}

	@Test
	fun `test encode Double`() {
		val old = 1.0
		val new = 2.0
		val change = Change(old, new)

		val document = serializeChange(change)
		assertEquals(2, document.size)
		assertEquals(old, document.getDouble("old").value)
		assertEquals(new, document.getDouble("new").value)

		val parsedChange = deserializeChange(document)
		assertEquals(change, parsedChange)
	}
}
