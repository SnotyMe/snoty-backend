package me.snoty.backend.scheduling

import me.snoty.node.schedule.ScheduleNodesSyncHooksRegistrar
import org.koin.core.Koin
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class SchedulerInterceptor(
    @Named("adapter") private val delegate: Scheduler,
    private val koin: Koin,
) : Scheduler by delegate {
    override fun start() {
        delegate.start()
        koin.get<ScheduleNodesSyncHooksRegistrar>().registerScheduleNodeHooks()
    }
}
