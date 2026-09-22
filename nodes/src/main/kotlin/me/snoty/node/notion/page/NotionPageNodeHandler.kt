package me.snoty.node.notion.page

import io.ktor.http.*
import io.ktor.server.response.*
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import me.snoty.backend.utils.BadRequestException
import me.snoty.backend.utils.respondStatus
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.data.get
import me.snoty.backend.wiring.node.*
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.backend.wiring.node.persistence.NodePersistenceFactory
import me.snoty.backend.wiring.node.persistence.invoke
import me.snoty.backend.wiring.node.routing.NodeHandlerRouteFactory
import me.snoty.backend.wiring.node.state.DiffResult
import me.snoty.core.node.NodeWithSettings
import me.snoty.node.notion.NOTION_BASE_URL
import me.snoty.node.notion.NotionAPIFactory
import me.snoty.node.notion.NotionConfig
import me.snoty.node.notion.oauth.NotionOAuth
import org.bson.Document
import org.koin.core.annotation.Single

@RegisterNode(
	name = "notion_page",
	displayName = "Notion Page",
	icon = Icon(name = "logos-notion-icon"),
	stereotype = NodeStereotype.END,
	settingsType = NotionPageSettings::class,
	inputType = NotionPage::class,
)
@Single
class NotionPageNodeHandler(
	private val apiFactory: NotionAPIFactory,
	persistenceFactory: NodePersistenceFactory,
	nodeHandlerRouteFactory: NodeHandlerRouteFactory,
	private val oauth: NotionOAuth,
	notionConfig: NotionConfig,
) : NodeHandler {
	@Serializable
	data class Page(val externalId: String, val notionId: String)

	private val pageService = persistenceFactory<Page>("pages")

	init {
		val authUrl = URLBuilder("$NOTION_BASE_URL/oauth/authorize")
			.apply {
				parameters["client_id"] = notionConfig.clientId
				parameters["redirect_uri"] = oauth.redirectUri
				parameters["response_type"] = "code"
				parameters["owner"] = "user"
			}
			.build()

		nodeHandlerRouteFactory("authorize", HttpMethod.Get, authenticated = false) {
			call.respondRedirect(authUrl)
		}

		nodeHandlerRouteFactory("callback", HttpMethod.Get, authenticated = false) {
			val code = call.parameters["code"]
				?: return@nodeHandlerRouteFactory call.respondStatus(BadRequestException("Missing code parameter"))

			val token = oauth.exchangeToken(code)
			call.respondText(token)
		}
	}

	context(_: NodeHandleContext)
	override suspend fun process(node: NodeWithSettings, input: NodeInput): NodeOutput {
		val settings = node.settings as NotionPageSettings
		val api = apiFactory(settings.token)

		val pages = pageService.getEntities(node).toList()
		input.forEach {
			val data = it.get<NotionPage>()
			val diff = it.get<Document>()["diff"] as? DiffResult
				?: error("No diff included in the input for ${data.id} - did you forget to add a DiffInjector node?")

			suspend fun create() {
				api.createPage(data.toPageCreateDTO(parent = settings.parent))
					.also { response ->
						pageService.persistEntity(
							node = node,
							entityId = data.id,
							entity = Page(externalId = data.id, notionId = response.id),
						)
					}
				logger.info("Created page: {}", data.id)
			}

			suspend fun update() {
				pages.getNotionId(data.id)
					?.let { pageId ->
						api.updatePage(pageId, data.toPageUpdateDTO())
						logger.info("Updated page: {}", data.id)
					}
					?: create()
			}

			suspend fun delete() {
				val pageId = pages.getNotionId(data.id)
					?: run {
						logger.error("Page for entity {} not found", data.id)
						return
					}

				if (settings.archiveOnDeletion) {
					api.deletePage(pageId)
					logger.info("Deleted page: {}", data.id)
				}

				pageService.deleteEntity(node, data.id)
			}

			when (diff) {
				is DiffResult.Created -> create()
				is DiffResult.Updated -> update()
				is DiffResult.Deleted -> delete()
				is DiffResult.Unchanged -> {}
			}
		}

		return emptyList()
	}

	private fun List<Page>.getNotionId(externalId: String) =
		firstOrNull { it.externalId == externalId }?.notionId
}
