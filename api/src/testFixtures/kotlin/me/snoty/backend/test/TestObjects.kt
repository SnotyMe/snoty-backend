package me.snoty.backend.test

import io.mockk.mockk
import me.snoty.backend.config.*
import me.snoty.backend.database.mongo.apiCodecModule
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.utils.bsonTypeClassMap
import me.snoty.integration.common.utils.integrationsApiCodecModule
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistryImpl
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateDataMapper
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateDataMapper
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import kotlin.reflect.KClass

val TestConfig = Config(
	port = 8080,
	environment = Environment.TEST,
	publicHost = "http://localhost:8080",
	mongodb = mockk(),
	featureFlags = FeatureFlagsConfig(ProviderFeatureFlagConfig.InMemory()),
	authentication = OidcConfig(
		serverUrl = "http://localhost:8081",
		clientId = "",
		clientSecret = ""
	)
)

class TestConfigBuilder(block: TestConfigBuilder.() -> Unit) {
	var port: Short = 8080
	var environment: Environment = Environment.TEST
	var publicHost: String = "http://localhost:8080"
	var mongodb: MongoConfig = mockk()
	var authentication: OidcConfig = OidcConfig(
		serverUrl = "http://localhost:8081",
		clientId = "",
		clientSecret = ""
	)

	init {
		block()
	}

	fun build() = Config(
		port = port,
		environment = environment,
		publicHost = publicHost,
		mongodb = mongodb,
		authentication = authentication,
		featureFlags = FeatureFlagsConfig(ProviderFeatureFlagConfig.InMemory())
	)
}

fun buildTestConfig(block: TestConfigBuilder.() -> Unit)
	= TestConfigBuilder(block).build()

val TestCodecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
	integrationsApiCodecModule(bsonTypeClassMap()),
	apiCodecModule()
)

val IntermediateDataMapperRegistry = IntermediateDataMapperRegistryImpl(
	listOf(
		BsonIntermediateDataMapper(TestCodecRegistry),
		SimpleIntermediateDataMapper()
	)
)

fun nodeMetadata(
	descriptor: NodeDescriptor,
	position: NodePosition = NodePosition.MIDDLE,
	settingsClass: KClass<out NodeSettings> = EmptyNodeSettings::class,
) = NodeMetadata(
	descriptor = descriptor,
	displayName = "Test Node",
	position = position,
	settings = emptyList(),
	settingsClass = settingsClass,
	input = null,
	output = null
)
