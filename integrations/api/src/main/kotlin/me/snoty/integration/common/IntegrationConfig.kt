package me.snoty.integration.common

import java.util.*

data class IntegrationConfig<S : IntegrationSettings>(val user: UUID, val settings: S)
