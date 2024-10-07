package me.snoty.integration.untis.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UntisMasterData(
	@SerialName("klassen")
	val classes: List<LongMasterElement>,
	val teachers: List<TeacherMasterElement>,
	val subjects: List<LongMasterElement>,
	val rooms: List<LongMasterElement>,
)

fun UntisMasterData.map(): MappedMasterData {
	val classes = classes.associateBy { it.id }
	val teachers = teachers.associateBy { it.id }
	val subjects = subjects.associateBy { it.id }
	val rooms = rooms.associateBy { it.id }
	return MappedMasterData(classes, teachers, subjects, rooms)
}

interface MasterElement {
	val id: Int
	val name: String
}

@Serializable
data class LongMasterElement(
	override val id: Int,
	override val name: String,
	val longName: String,
) : MasterElement

@Serializable
data class TeacherMasterElement(
	override val id: Int,
	override val name: String,
	val firstName: String,
	val lastName: String,
) : MasterElement

data class MappedMasterData(
	val classes: Map<Int, LongMasterElement>,
	val teachers: Map<Int, TeacherMasterElement>,
	val subjects: Map<Int, LongMasterElement>,
	val rooms: Map<Int, LongMasterElement>,
)
