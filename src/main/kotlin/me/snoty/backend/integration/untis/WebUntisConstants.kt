package me.snoty.backend.integration.untis

object UntisApiConstants {
	const val DEFAULT_WEBUNTIS_HOST = "mobile.webuntis.com"
	const val DEFAULT_WEBUNTIS_PATH = "/ms/app/"
	const val SCHOOL_SEARCH_URL = "https://schoolsearch.webuntis.com/schoolquery2"

	object Method {
		const val CREATE_IMMEDIATE_ABSENCE = "createImmediateAbsence2017"
		const val DELETE_ABSENCE = "deleteAbsence2017"
		const val GET_ABSENCES = "getStudentAbsences2017"
		const val GET_APP_SHARED_SECRET = "getAppSharedSecret"
		const val GET_EXAMS = "getExams2017"
		const val GET_HOMEWORKS = "getHomeWork2017"
		const val GET_MESSAGES = "getMessagesOfDay2017"
		const val GET_OFFICEHOURS = "getOfficeHours2017"
		const val GET_PERIOD_DATA = "getPeriodData2017"
		const val GET_TIMETABLE = "getTimetable2017"
		const val GET_USER_DATA = "getUserData2017"
		const val SEARCH_SCHOOLS = "searchSchool"
		const val SUBMIT_ABSENCES_CHECKED = "submitAbsencesChecked2017"
		const val GET_LESSON_TOPIC = "getLessonTopic2017"
		const val SUBMIT_LESSON_TOPIC = "submitLessonTopic"
	}

	object Permission {
		const val CAN_READ_STUDENT_ABSENCE = "READ_STUD_ABSENCE"
		const val CAN_WRITE_STUDENT_ABSENCE = "WRITE_STUD_ABSENCE"
		const val CAN_READ_LESSON_TOPIC = "READ_LESSONTOPIC"
		const val CAN_WRITE_LESSON_TOPIC = "WRITE_LESSONTOPIC"
		const val CAN_READ_HOMEWORK = "READ_HOMEWORK"
		const val CAN_WRITE_HOMEWORK = "WRITE_HOMEWORK"
		const val CAN_READ_CLASSREG_EVENT = "READ_CLASSREGEVENT"
		const val CAN_WRITE_CLASSREG_EVENT = "WRITE_CLASSREGEVENT"
		const val CAN_DELETE_CLASSREG_EVENT = "DELETE_CLASSREGEVENT"
		const val CAN_READ_CLASS_ROLE = "READ_CLASSROLE"
		const val CAN_READ_PERIOD_INFO = "READ_PERIODINFO"
		const val CAN_WRITE_PERIOD_INFO = "WRITE_PERIODINFO"
		const val CAN_ACTION_CHANGE_ROOM = "ACTION_CHANGE_ROOM"

		const val RIGHT_OFFICEHOURS = "R_OFFICEHOURS"
		const val RIGHT_ABSENCES = "R_MY_ABSENCES"
		const val RIGHT_CLASSREGISTER = "CLASSREGISTER"
	}
}
