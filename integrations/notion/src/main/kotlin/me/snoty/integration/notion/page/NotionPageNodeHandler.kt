package me.snoty.integration.notion.page

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.response.*
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import me.snoty.backend.utils.BadRequestException
import me.snoty.backend.utils.respondStatus
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.get
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeHandlerRouteFactory
import me.snoty.integration.common.wiring.node.NodePersistenceFactory
import me.snoty.integration.common.wiring.node.invoke
import me.snoty.integration.notion.NOTION_BASE_URL
import me.snoty.integration.notion.NotionAPI
import me.snoty.integration.notion.NotionAPIImpl
import me.snoty.integration.notion.NotionConfig
import me.snoty.integration.notion.oauth.NotionOAuth
import org.bson.Document
import org.koin.core.annotation.Single

@RegisterNode(
	name = "notion_page",
	displayName = "Notion Page",
	position = NodePosition.END,
	settingsType = NotionPageSettings::class,
	inputType = NotionPage::class,
)
@Single
class NotionPageNodeHandler(
	httpClient: HttpClient,
	private val apiFactory: (token: String) -> NotionAPI = { NotionAPIImpl(httpClient, it) },
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

	override suspend fun NodeHandleContext.process(node: Node, input: Collection<IntermediateData>): NodeOutput {
		val settings = node.settings as NotionPageSettings
		val api = apiFactory(settings.token)

		val pages = pageService.getEntities(node).toList()
		input.forEach {
			val data = get<NotionPage>(it)
			val diff = get<Document>(it)["diff"] as? DiffResult
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
