package me.snoty.backend.test

import me.snoty.core.UserId
import kotlin.uuid.Uuid

object TestIds {
	val USER_ID_1 = UserId(Uuid.fromLongs(1, 0).toString())
	val USER_ID_2 = UserId(Uuid.fromLongs(2, 0).toString())
	val USER_ID_CONTROL = UserId(Uuid.fromLongs(64, 0).toString())
	const val INTEGRATION_NAME = "moodle"
	const val ENTITY_TYPE = "exam"
	const val ENTITY_TYPE_CONTROL = "notexam"
}
