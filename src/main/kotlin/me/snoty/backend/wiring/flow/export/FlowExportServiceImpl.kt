package me.snoty.backend.wiring.flow.export

import me.snoty.backend.schema.ObjectSchema
import me.snoty.backend.schema.SchemaFieldDetails
import me.snoty.backend.utils.bson.encode
import me.snoty.backend.utils.bson.setByPath
import me.snoty.backend.wiring.flow.*
import me.snoty.backend.wiring.node.NodeSettings
import me.snoty.backend.wiring.node.registry.NodeRegistry
import me.snoty.core.flow.Workflow
import me.snoty.core.node.NodeId
import me.snoty.core.node.NodeType
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

@Single
class FlowExportServiceImpl(
	private val flowService: FlowService,
	private val codecRegistry: CodecRegistry,
	private val nodeRegistry: NodeRegistry,
) : FlowExportService {
	override suspend fun export(flow: Workflow, censor: Boolean): ExportFlow {
		val flow = flowService.getWithNodes(flow.userId, flow.id) ?: throw IllegalArgumentException("Flow not found")
		return ExportFlow(
			version = FlowExportImportSchema.VERSION,
			templateName = flow.name,
			settings = flow.settings,
			nodes = flow.nodes.map {
				val settings = it.settings.encode(it.type, censor)
				ExportNode(
					id = it.id.hash(),
					type = it.type,
					name = it.name,
					position = it.position,
					settings = settings,
					next = it.next.map(NodeId::hash),
				)
			}
		)
	}

	private fun NodeSettings.encode(nodeType: NodeType, censor: Boolean): Document = when {
		censor -> censorSettings(nodeType, this)
		else -> codecRegistry.encode(this)
	}

	private fun censorSettings(nodeType: NodeType, settings: NodeSettings): Document {
		val metadata = nodeRegistry.getMetadata(nodeType)
		val encoded = codecRegistry.encode(settings)
		encoded.censorRecursively(metadata.settings)

		return encoded
	}

	private fun Document.censorRecursively(fields: ObjectSchema, parts: Array<String> = emptyArray()) {
		fields.forEach { field ->
			val replacement = when {
				field.details is SchemaFieldDetails.CredentialDetails -> null
				field.censored -> CensoredField(field.name)
				else -> return@forEach
			}
			val pathKey = (parts + field.name).joinToString(".")
			this.setByPath(pathKey, replacement)
		}

		fields
			.forEach { field ->
				val details = field.details
				if (details is SchemaFieldDetails.ObjectDetails)
					this.censorRecursively(details.schema, parts + field.name)
			}
	}
}
