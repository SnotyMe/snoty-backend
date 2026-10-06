import type { JobSchedule, JobScheduleCron } from "~/api/backend/generated"
import cronstrue from "cronstrue/i18n"

export const useFlowSchedule = () => {
	const { locale } = useI18n()

	function formatSchedule(schedule: JobSchedule) {
		switch (schedule.type) {
			case "never":
				return "never"
			case "recurring":
				return "recurring"
			case "cron":
				return formatCronSchedule(schedule)
		}
	}

	function formatCronSchedule(schedule: JobScheduleCron) {
		const formatted = cronstrue.toString(schedule.expression, { locale: locale.value })

		return formatted.charAt(0).toLowerCase() + formatted.substring(1)
	}

	return {
		formatSchedule,
	}
}
