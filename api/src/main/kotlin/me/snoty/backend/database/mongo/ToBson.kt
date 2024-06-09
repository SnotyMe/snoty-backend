package me.snoty.backend.database.mongo

import org.bson.BsonDocument
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

interface ToBson : Bson {
	fun toDocument(): Document

	override fun <TDocument : Any?> toBsonDocument(
		documentClass: Class<TDocument?>?,
		codecRegistry: CodecRegistry?
	): BsonDocument? {
		return toDocument().toBsonDocument(documentClass, codecRegistry)
	}
}

interface ToBsonBuilder : ToBson {
	fun buildDocument(document: Document)

	override fun toDocument(): Document = Document()
		.also {
			buildDocument(it)
		}
}
