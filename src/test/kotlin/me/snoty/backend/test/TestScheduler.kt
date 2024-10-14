package me.snoty.backend.test

import me.snoty.backend.scheduling.Scheduler
import me.snoty.backend.scheduling.SnotyJob

class TestScheduler : Scheduler {
	override fun scheduleJob(job: SnotyJob) {
		throw NotImplementedError()
	}

	override fun scheduleRecurringJob(id: String, job: SnotyJob) {
		throw NotImplementedError()
	}
}
