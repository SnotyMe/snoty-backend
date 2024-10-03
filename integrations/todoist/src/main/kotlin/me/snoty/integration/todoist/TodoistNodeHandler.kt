package me.snoty.integration.todoist

import io.ktor.client.*
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.FieldCensored
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.get
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodePersistenceFactory
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.node.invoke
import org.bson.Document
import org.koin.core.annotation.Single

@Serializable
data class TodoistSettings(
	override val name: String = "Todoist",
	@FieldCensored
	val token: String,
	val projectId: String?,
	val sectionId: String?,
) : NodeSettings

@RegisterNode(
	type = "todoist",
	displayName = "Todoist",
	position = NodePosition.END,
	settingsType = TodoistSettings::class,
	inputType = TodoistInput::class,
)
@Single
class TodoistNodeHandler(
	private val httpClient: HttpClient,
	private val apiFactory: (String) -> TodoistAPI = { TodoistAPIImpl(httpClient, it) },
	persistenceFactory: NodePersistenceFactory,
) : NodeHandler {
	data class Task(val externalId: String, val todoistId: String)

	private val taskService = persistenceFactory<Task>("tasks")

	override suspend fun NodeHandleContext.process(node: Node, input: Collection<IntermediateData>): NodeOutput {
		val settings = node.settings as TodoistSettings
		val api = apiFactory(settings.token)

		val tasks = taskService.getEntities(node).toList()
		input.forEach {
			val data = get<TodoistInput>(it)
			val diff = get<Document>(it)["diff"] as DiffResult

			suspend fun create() {
				api.createTask(data.toTaskCreateDTO(
					projectId = settings.projectId,
					sectionId = settings.sectionId,
				))
					.also { response ->
						taskService.persistEntity(
							node = node,
							entityId = data.id,
							entity = Task(externalId = data.id, todoistId = response.id),
						)
					}
				logger.info("Created task: {}", data.content)
			}

			when (diff) {
				is DiffResult.Created -> create()
				is DiffResult.Updated -> {
					tasks.getTodoistId(data.id)
						?.let { taskId ->
							api.updateTask(taskId, data.toTaskUpdateDTO())
							logger.info("Updated task: {}", data.content)
						}
						?: create()
				}
				is DiffResult.Deleted -> {
					val taskId = tasks.getTodoistId(data.id)
						?: run {
							logger.error("Task {} for entity {} not found", data.content, data.id)
							return@forEach
						}
					api.closeTask(taskId)
					logger.info("Closed task: {}", data.content)
				}
				is DiffResult.Unchanged -> {}
			}
		}

		return emptyList()
	}

	private fun List<Task>.getTodoistId(externalId: String) =
		firstOrNull { it.externalId == externalId }?.todoistId
}
