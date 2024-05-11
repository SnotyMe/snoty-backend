package me.snoty.backend.test

import me.snoty.backend.scheduling.Scheduler

class TestScheduler : Scheduler {
	override fun scheduleJob(id: String, job: () -> Unit) {
		job()
	}
}
