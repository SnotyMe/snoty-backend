package me.snoty.integration.common.fetch

import org.jobrunr.jobs.context.JobDashboardProgressBar
import org.slf4j.Logger

enum class IntegrationProgressState {
	INIT,
	FETCHING,
	UPDATING_IN_DB,
	FLOWING,
	STAGE_DONE;

	companion object {
		// states per stage
		// `exams`, `assignments` are stages, `FETCHING` and `UPDATING_IN_DB` the states
		private val STATE_PER_STAGE_COUNT = entries.size - 1

		// everything that isn't stage scoped is added to the ones from the stages
		fun getStateCount(stages: Long) = entries.size - STATE_PER_STAGE_COUNT + STATE_PER_STAGE_COUNT * stages
	}

	val progress = ordinal + 1L
}

interface FetchProgress {
	fun advance(state: IntegrationProgressState)
}

class JobRunrFetchProgress(private val logger: Logger, private val progressBar: JobDashboardProgressBar) : FetchProgress {
	private var currentStage = 0
	// stores the last state - to notice when the next stage is here
	private var lastState = IntegrationProgressState.INIT

	init {
		advance(IntegrationProgressState.INIT)
	}

	override fun advance(state: IntegrationProgressState) {
		// new progress is smaller than last progress - we've jumped to the next stage
		if (state.progress < lastState.progress) {
			// we've advanced to the next stage
			currentStage++
		}
		lastState = state
		logger.info("[${currentStage}] ${state.name}")
		progressBar.setProgress(state.progress * currentStage)
	}
}
