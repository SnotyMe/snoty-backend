package me.snoty.backend.wiring.flow.export

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.utils.bson.encode
import me.snoty.backend.utils.bson.setByPath
import me.snoty.backend.wiring.flow.CensoredField
import me.snoty.backend.wiring.flow.ExportFlow
import me.snoty.backend.wiring.flow.ExportNode
import me.snoty.backend.wiring.flow.FlowExportImportSchema
import me.snoty.integration.common.model.metadata.NodeField
import me.snoty.integration.common.model.metadata.NodeFieldDetails
import me.snoty.integration.common.model.metadata.ObjectSchema
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

@Single
class FlowExportServiceImpl(
	private val flowService: FlowService,
	private val codecRegistry: CodecRegistry,
	private val nodeRegistry: NodeRegistry,
) : FlowExportService {
	override suspend fun export(flowId: NodeId, censor: Boolean): ExportFlow {
		val flow = flowService.getWithNodes(flowId) ?: throw IllegalArgumentException("Flow not found")
		return ExportFlow(
			version = FlowExportImportSchema.VERSION,
			templateName = flow.name,
			settings = flow.settings,
			nodes = flow.nodes.map {
				val settings = it.settings.encode(it.descriptor, censor)
				ExportNode(
					id = it._id.hash(),
					descriptor = it.descriptor,
					settings = settings,
					next = it.next.map(NodeId::hash),
				)
			}
		)
	}

	private fun NodeSettings.encode(descriptor: NodeDescriptor, censor: Boolean): Document = when {
		censor -> censorSettings(descriptor, this)
		else -> codecRegistry.encode(this)
	}

	private fun censorSettings(descriptor: NodeDescriptor, settings: NodeSettings): Document {
		val metadata = nodeRegistry.getMetadata(descriptor)
		val encoded = codecRegistry.encode(settings)
		encoded.censorRecursively(metadata.settings)

		return encoded
	}

	private fun Document.censorRecursively(fields: ObjectSchema, parts: Array<String> = emptyArray()) {
		fields
			.filter(NodeField::censored)
			.forEach { field ->
				val pathKey = (parts + field.name).joinToString(".")
				this.setByPath(pathKey, CensoredField(default = field.defaultValue))
			}

		fields
			.forEach { field ->
				val details = field.details
				if (details is NodeFieldDetails.ObjectDetails)
					this.censorRecursively(details.schema, parts + field.name)
			}
	}
}
