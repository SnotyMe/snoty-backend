package me.snoty.backend.integration.moodle

object MoodleApiConstants {
	object Function {
		object Core {
			object Calendar {
				const val GET_UPCOMING_VIEW = "core_calendar_get_calendar_upcoming_view"
			}
			object User {
				const val GET_BY_FIELD = "core_user_get_users_by_field"
			}
		}
	}
}
