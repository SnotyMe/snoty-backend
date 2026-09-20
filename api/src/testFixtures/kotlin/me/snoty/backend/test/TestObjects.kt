package me.snoty.backend.test

import me.snoty.backend.config.Config
import me.snoty.backend.config.Environment
import me.snoty.backend.utils.bson.bsonTypeClassMap
import me.snoty.backend.utils.bson.provideApiCodec
import me.snoty.backend.utils.bson.provideCodecRegistry
import me.snoty.backend.wiring.data.IntermediateDataMapperRegistryImpl
import me.snoty.backend.wiring.data.impl.BsonIntermediateDataMapper
import me.snoty.backend.wiring.data.impl.SimpleIntermediateDataMapper
import me.snoty.backend.wiring.node.EmptyNodeSettings
import me.snoty.backend.wiring.node.NodeSettings
import me.snoty.backend.wiring.node.metadata.NodeMetadata
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeType
import org.bson.Document
import org.bson.codecs.DocumentCodec
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import kotlin.reflect.KClass

val TestConfig = Config(
	port = 8080,
	environment = Environment.TEST,
	publicHost = "http://localhost:8080",
)

class TestConfigBuilder(block: TestConfigBuilder.() -> Unit) {
	var port: Short = 8080
	var environment: Environment = Environment.TEST
	var publicHost: String = "http://localhost:8080"

	init {
		block()
	}

	fun build() = Config(
		port = port,
		environment = environment,
		publicHost = publicHost,
	)
}

fun buildTestConfig(block: TestConfigBuilder.() -> Unit)
	= TestConfigBuilder(block).build()

val TestCodecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
	provideCodecRegistry(provideApiCodec(bsonTypeClassMap()))
)

val IntermediateDataMapperRegistry = IntermediateDataMapperRegistryImpl(
	listOf(
		BsonIntermediateDataMapper(TestCodecRegistry, TestCodecRegistry.get(Document::class.java) as DocumentCodec),
		SimpleIntermediateDataMapper()
	)
)

fun nodeMetadata(
	type: NodeType,
	stereotype: NodeStereotype = NodeStereotype.MIDDLE,
	settingsClass: KClass<out NodeSettings> = EmptyNodeSettings::class,
	receiveEmptyInput: Boolean = false,
) = NodeMetadata(
	type = type,
	displayName = "Test Node",
	stereotype = stereotype,
	settings = emptyList(),
	settingsClass = settingsClass,
	receiveEmptyInput = receiveEmptyInput,
	input = null,
	output = null
)

fun nodeMetadata(
	name: String,
	stereotype: NodeStereotype = NodeStereotype.MIDDLE,
	settingsClass: KClass<out NodeSettings> = EmptyNodeSettings::class,
	receiveEmptyInput: Boolean = false,
) = nodeMetadata(
	type = NodeType(name),
	stereotype,
	settingsClass,
	receiveEmptyInput,
)
