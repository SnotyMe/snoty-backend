package me.snoty.backend.test

import io.mockk.mockk
import io.mockk.mockkClass
import kotlinx.datetime.Clock
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.config.*
import me.snoty.backend.database.mongo.apiCodecModule
import me.snoty.backend.injection.ServicesContainer
import me.snoty.backend.integration.flow.FlowBuilderImpl
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.utils.integrationsApiCodecModule
import me.snoty.integration.common.wiring.NodeHandlerContext
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistry
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateDataMapper
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateDataMapper
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
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

val TestBuildInfo = BuildInfo(
	gitBranch = "<test>",
	gitCommit = "<test>",
	gitCommitDate = Clock.System.now(),
	buildDate = Clock.System.now(),
	version = "<test>",
	application = "snoty-backend"
)

val TestCodecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
	integrationsApiCodecModule(),
	apiCodecModule()
)

val MockNodeHandlerContext = NodeHandlerContext(
	entityStateService = mockk(),
	nodeService = mockk(),
	flowService = mockk(),
	codecRegistry = mockk(),
	calendarService = mockk(),
	intermediateDataMapperRegistry = IntermediateDataMapperRegistry().apply {
		this[BsonIntermediateData::class] = BsonIntermediateDataMapper(TestCodecRegistry)
		this[SimpleIntermediateData::class] = SimpleIntermediateDataMapper
	},
	scheduler = mockk(),
	openTelemetry = mockk()
)

val TestFlowBuilder = FlowBuilderImpl {
	EmptyNodeSettings()
}

val MockServicesContainer = object : ServicesContainer {
	override fun <T : Any> register(clazz: KClass<T>, instance: T) = throw UnsupportedOperationException()
	override fun <T : Any> register(instance: T) = throw UnsupportedOperationException()

	override fun <T : Any> get(clazz: KClass<T>): T = mockkClass(clazz)
}

val TestNodeMetadata = NodeMetadata(
	displayName = "Test Node",
	position = NodePosition.MIDDLE,
	settings = emptyList(),
	input = null,
	output = null
)
