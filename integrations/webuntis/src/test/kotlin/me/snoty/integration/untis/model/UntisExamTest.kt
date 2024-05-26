package me.snoty.integration.untis.model

import kotlinx.datetime.Clock
import me.snoty.integration.common.diff.AbstractDiffTest
import me.snoty.integration.common.diff.Change
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.untis.WebUntisIntegration.Companion.untisCodecModule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class UntisExamTest : AbstractDiffTest(untisCodecModule) {

	private val baseExam = UntisExam(
		id = 20,
		examType = "Test",
		startDateTime = UntisDateTime(Clock.System.now().plus(10.minutes)),
		endDateTime = UntisDateTime(Clock.System.now().plus(50.minutes)),
		departmentId = 10,
		subjectId = 11,
		klasseIds = listOf(12),
		roomIds = listOf(13),
		teacherIds = listOf(14),
		name = "My Test",
		text = "description"
	)

	@Test
	fun testFullCircle() {
		var exam = baseExam
		var doc = encodeDocument(exam)
		assertEquals(DiffResult.Unchanged, exam.diff(doc))

		exam = exam.copy(endDateTime = UntisDateTime(Clock.System.now().plus(3.hours)))
		doc = encodeDocument(exam)
		assertEquals(DiffResult.Unchanged, exam.diff(doc))

		exam = exam.copy(name = "Testing")
		doc = encodeDocument(exam)
		assertEquals(DiffResult.Unchanged, exam.diff(doc))
	}

	@Test
	fun testDateDiffs() {
		val newEnd = UntisDateTime(Clock.System.now().plus(55.minutes))
		var newExam = baseExam.copy(endDateTime = newEnd)
		assertNotEquals(baseExam.checksum, newExam.checksum)
		var ogDiff = assertDoesNotThrow {
			baseExam.diff(encodeDocument(newExam))
		}
		var diff = assertInstanceOf(DiffResult.Updated::class.java, ogDiff).diff
		assertEquals(1, diff.size)
		var entry = diff.entries.first()
		assertEquals("endDateTime", entry.key)
		assertEquals(Change(baseExam.endDateTime, newEnd), entry.value)

		val newStart = UntisDateTime(Clock.System.now().plus(15.minutes))
		newExam = baseExam.copy(startDateTime = newStart)
		assertNotEquals(baseExam.checksum, newExam.checksum)
		ogDiff = assertDoesNotThrow {
			baseExam.diff(encodeDocument(newExam))
		}
		diff = assertInstanceOf(DiffResult.Updated::class.java, ogDiff).diff
		assertEquals(1, diff.size)
		entry = diff.entries.first()
		assertEquals("startDateTime", entry.key)
		assertEquals(Change(baseExam.startDateTime, newStart), entry.value)
	}

	@Test
	fun testTextDiff() {
		val newText = "new"
		val newExam = baseExam.copy(text = newText)
		assertNotEquals(baseExam.checksum, newExam.checksum)
		val ogDiff = assertDoesNotThrow {
			baseExam.diff(encodeDocument(newExam))
		}
		val diff = assertInstanceOf(DiffResult.Updated::class.java, ogDiff).diff
		assertEquals(1, diff.size)
		val entry = diff.entries.first()
		assertEquals("text", entry.key)
		assertEquals(Change(baseExam.text, newText), entry.value)
	}
}
