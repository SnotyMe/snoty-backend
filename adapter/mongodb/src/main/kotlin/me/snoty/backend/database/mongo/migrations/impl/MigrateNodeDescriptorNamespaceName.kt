package me.snoty.backend.database.mongo.migrations.impl

import com.mongodb.MongoCommandException
import com.mongodb.MongoNamespace
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateManyModel
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import me.snoty.backend.database.mongo.migrations.MongoMigration
import me.snoty.integration.common.wiring.flow.NODE_COLLECTION_NAME
import me.snoty.integration.common.wiring.graph.MongoNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.bson.Document
import org.koin.core.annotation.Single

@Single
class MigrateNodeDescriptorNamespaceName : MongoMigration("0.0.2") {
	override val name: String = "NodeDescriptorNamespaceName"
	override val description: String = "migrate NodeDescriptor from subsystem:type to namespace:name"
	private val collectionRenames = mapOf(
		"filter.unchangedfilter.entityStates" to "me.snoty.integration.builtin.diff.unchangedfilter:unchangedfilter:entityStates",
		"processor.diffinjector.entityStates" to "me.snoty.integration.builtin.diff.injector:diffinjector:entityStates",
		"integration.ical.events" to "me.snoty.integration.builtin.calendar:ical:events",
		"integration.notion_page.pages" to "me.snoty.integration.notion.page:notion_page:pages",
		"integration.todoist.tasks" to "me.snoty.integration.todoist:todoist:tasks"
	).mapValues { (_, new) -> "nodes:$new" }

	private val descriptorRemaps = mapOf(
		"integration:ical" to NodeDescriptor("me.snoty.integration.builtin.calendar", "ical"),
		"processor:diffinjector" to NodeDescriptor("me.snoty.integration.builtin.diff.injector", "diffinjector"),
		"filter:unchangedfilter" to NodeDescriptor("me.snoty.integration.builtin.diff.unchangedfilter", "unchangedfilter"),
		"processor:unidiff" to NodeDescriptor("me.snoty.integration.builtin.diff.uni", "unidiff"),
		"integration:emitjson" to NodeDescriptor("me.snoty.integration.builtin.emitjson", "emitjson"),
		"processor:mapper" to NodeDescriptor("me.snoty.integration.builtin.mapper", "mapper"),
		"integration:discord" to NodeDescriptor("me.snoty.integration.discord", "discord"),
		"integration:mail" to NodeDescriptor("me.snoty.integration.mail.global", "mail"),
		"integration:smtp" to NodeDescriptor("me.snoty.integration.mail.smtp", "smtp"),
		"integration:moodle" to NodeDescriptor("me.snoty.integration.moodle", "moodle_assignments"),
		"integration:notion_page" to NodeDescriptor("me.snoty.integration.notion.page", "notion_page"),
		"integration:todoist" to NodeDescriptor("me.snoty.integration.todoist", "todoist"),
		"integration:webuntis_exams" to NodeDescriptor("me.snoty.integration.untis.node.exam", "webuntis_exams"),
		"integration:webuntis_timetable" to NodeDescriptor("me.snoty.integration.untis.node.timetable", "webuntis_timetable"),
		"integration:ai_prompt" to NodeDescriptor("me.snoty.integration.contrib.ai.prompt", "ai_prompt"),
		"integration:studyly" to NodeDescriptor("me.snoty.integration.contrib.studyly", "studyly"),
		"integration:willhaben_listing" to NodeDescriptor("me.snoty.integration.contrib.willhaben.listing", "willhaben_listing")
	)

	private suspend fun MongoDatabase.tryRename(oldName: String, newName: String) {
		try {
			getCollection<Document>(oldName)
				.renameCollection(MongoNamespace(this.name, newName))
		} catch (e: MongoCommandException) {
			if (e.errorCodeName != "NamespaceNotFound") throw e
			logger.trace { "Collection $oldName not found, skipping rename" }
		}
	}

	override suspend fun execute(db: MongoDatabase) {
		collectionRenames.forEach { (oldName, newName) ->
			logger.debug { "Renaming collection $oldName to $newName" }
			db.tryRename(oldName, newName)
		}

		val collection = db.getCollection<MongoNode>(NODE_COLLECTION_NAME)
		collection.bulkWrite(descriptorRemaps.map { (old, new) ->
			val oldSplit = old.split(":")
			UpdateManyModel(
				Filters.and(
					Filters.eq("${MongoNode::descriptor.name}.subsystem", oldSplit[0]),
					Filters.eq("${MongoNode::descriptor.name}.type", oldSplit[1])
				),
				Updates.set(MongoNode::descriptor.name, new)
			)
		})
	}

	override suspend fun rollback(db: MongoDatabase) {
		collectionRenames.forEach { (oldName, newName) ->
			logger.debug { "Rolling back rename of collection by renaming $newName to $oldName" }
			db.tryRename(newName, oldName)
		}

		val collection = db.getCollection<MongoNode>(NODE_COLLECTION_NAME)
		collection.bulkWrite(descriptorRemaps.map { (old, new) ->
			val oldSplit = old.split(":")
			UpdateManyModel(
				Filters.eq(
					MongoNode::descriptor.name, mapOf(
						"namespace" to new.namespace,
						"name" to new.name
					)
				),
				Updates.set(
					MongoNode::descriptor.name, mapOf(
						"subsystem" to oldSplit[0],
						"type" to oldSplit[1]
					)
				)
			)
		})
	}
}
