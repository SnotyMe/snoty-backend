package me.snoty.backend.test

import me.snoty.backend.config.Config
import me.snoty.backend.config.Environment
import me.snoty.backend.config.FeatureFlagsConfig
import me.snoty.backend.config.ProviderFeatureFlagConfig
import me.snoty.backend.utils.bson.provideApiCodec
import me.snoty.backend.utils.bson.provideCodecRegistry
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.utils.bsonTypeClassMap
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistryImpl
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateDataMapper
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateDataMapper
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.bson.codecs.DocumentCodec
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import kotlin.reflect.KClass

val TestConfig = Config(
	port = 8080,
	environment = Environment.TEST,
	publicHost = "http://localhost:8080",
	featureFlags = FeatureFlagsConfig(ProviderFeatureFlagConfig.InMemory()),
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
		featureFlags = FeatureFlagsConfig(ProviderFeatureFlagConfig.InMemory())
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
	descriptor: NodeDescriptor,
	position: NodePosition = NodePosition.MIDDLE,
	settingsClass: KClass<out NodeSettings> = EmptyNodeSettings::class,
	receiveEmptyInput: Boolean = false,
) = NodeMetadata(
	descriptor = descriptor,
	displayName = "Test Node",
	position = position,
	settings = emptyList(),
	settingsClass = settingsClass,
	receiveEmptyInput = receiveEmptyInput,
	input = null,
	output = null
)

fun nodeMetadata(
	name: String,
	namespace: String = "me.snoty.backend.test",
	position: NodePosition = NodePosition.MIDDLE,
	settingsClass: KClass<out NodeSettings> = EmptyNodeSettings::class,
	receiveEmptyInput: Boolean = false,
) = nodeMetadata(
	NodeDescriptor(
		namespace = namespace,
		name = name
	),
	position,
	settingsClass,
	receiveEmptyInput,
)
