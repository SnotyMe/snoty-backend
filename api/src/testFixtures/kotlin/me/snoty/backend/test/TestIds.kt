package me.snoty.backend.test

import kotlin.uuid.Uuid

object TestIds {
	val USER_ID_1 = Uuid.fromLongs(1, 0)
	val USER_ID_2 = Uuid.fromLongs(2, 0)
	val USER_ID_CONTROL = Uuid.fromLongs(64, 0)
	const val INTEGRATION_NAME = "moodle"
	const val ENTITY_TYPE = "exam"
	const val ENTITY_TYPE_CONTROL = "notexam"
}
