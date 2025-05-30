package me.snoty.integration.common.diff

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.DocumentCodec
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry

sealed class DiffResult {
	data object Unchanged : DiffResult()
	data class Created(val checksum: Long, val fields: Document) : DiffResult()
	data class Updated(val checksum: Long, val change: Diff) : DiffResult()
	data class Deleted(val checksum: Long, val previous: Document) : DiffResult()
}

class DiffResultCodec(
	private val codecRegistry: CodecRegistry,
	private val documentCodec: DocumentCodec,
) : Codec<DiffResult> {
	override fun encode(writer: BsonWriter, value: DiffResult, encoderContext: EncoderContext) = value.run {
		writer.writeStartDocument()
		writer.writeString("type", value::class.simpleName)
		when (this) {
			is DiffResult.Created -> {
				writer.writeInt64("checksum", checksum)
				writer.writeName("fields")
				documentCodec.encode(writer, fields, encoderContext)
			}
			is DiffResult.Updated -> {
				writer.writeInt64("checksum", checksum)
				writer.writeName("change")
				codecRegistry.get(change.javaClass).encode(writer, change, encoderContext)
			}
			is DiffResult.Deleted -> {
				writer.writeInt64("checksum", checksum)
				writer.writeName("previous")
				documentCodec.encode(writer, previous, encoderContext)
			}
			is DiffResult.Unchanged -> {}
		}
		writer.writeEndDocument()
	}

	override fun getEncoderClass(): Class<DiffResult> = DiffResult::class.java

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): DiffResult {
		val document = decoderContext.decodeWithChildContext(documentCodec, reader)
		return when (val type = document.getString("type")) {
			DiffResult.Created::class.simpleName -> DiffResult.Created(
				document.getLong("checksum"),
				document.get("fields", Document::class.java)
			)
			DiffResult.Updated::class.simpleName -> DiffResult.Updated(
				document.getLong("checksum"),
				document.get("change", Diff::class.java)
			)
			DiffResult.Deleted::class.simpleName -> DiffResult.Deleted(
				document.getLong("checksum"),
				document.get("previous", Document::class.java)
			)
			DiffResult.Unchanged::class.simpleName -> DiffResult.Unchanged
			else -> throw UnsupportedOperationException("Unsupported type: $type")
		}
	}
}
