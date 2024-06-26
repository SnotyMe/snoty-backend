package me.snoty.integration.common.fetch

import org.jobrunr.jobs.context.JobDashboardLogger
import org.jobrunr.jobs.context.JobDashboardProgressBar

interface FetchProgress {
	fun advance(state: IntegrationProgressState)
}

class FetchContext(private val progress: FetchProgress) : FetchProgress by progress {
	suspend fun <T> fetch(block: suspend () -> T): T {
		progress.advance(IntegrationProgressState.FETCHING)
		return block()
	}

	suspend fun updateStates(block: suspend () -> Unit) {
		progress.advance(IntegrationProgressState.UPDATING_IN_DB)
		block()
	}
}

enum class IntegrationProgressState {
	INIT,
	FETCHING,
	UPDATING_IN_DB,
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

class JobRunrFetchProgress(private val logger: JobDashboardLogger, private val progressBar: JobDashboardProgressBar) : FetchProgress {
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
