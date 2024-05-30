package me.snoty.backend.test

import me.snoty.backend.scheduling.Scheduler
import me.snoty.backend.scheduling.SnotyJob

class TestScheduler : Scheduler {
	override fun scheduleJob(id: String, job: () -> Unit) {
		job()
	}

	override fun scheduleJob(id: String, job: SnotyJob) {
		throw NotImplementedError()
	}
}
