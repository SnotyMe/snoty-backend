package me.snoty.integration.untis.request

import kotlinx.serialization.Serializable
import me.snoty.integration.untis.*
import me.snoty.integration.untis.helpers.toUserParams
import me.snoty.integration.untis.model.UntisDate
import me.snoty.integration.untis.model.UntisExam
import me.snoty.integration.untis.model.UntisUserData
import me.snoty.integration.untis.param.ExamParams
import java.time.LocalDate

suspend fun WebUntisAPI.getExams(userSettings: WebUntisSettings, userData: UntisUserData): List<UntisExam> {
	// will be moved to database as this information is static
	val request = UntisRequest(
		userSettings,
		UntisPayload {
			method = UntisApiConstants.Method.GET_EXAMS
			val userParams = userSettings.toUserParams()
			param(
				ExamParams(
					id = userData.id,
					type = userData.type,
					startDate = UntisDate(LocalDate.now()),
					endDate = UntisDate(LocalDate.now().plusMonths(6)),
					auth = userParams.auth
				)
			)
		},
	)

	@Serializable
	data class ExamsResponse(val exams: List<UntisExam>)

	return request<ExamsResponse>(request).exams
}
