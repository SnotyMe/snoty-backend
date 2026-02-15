package me.snoty.backend.wiring.flow.execution

import me.snoty.backend.adapter.Adapter

interface ExecutionEventAdapter : Adapter {
    companion object {
        const val CONFIG_GROUP = "executionevent"
    }
}
