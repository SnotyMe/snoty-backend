package me.snoty.extension

import org.koin.core.module.Module
import org.koin.core.scope.ScopeID

interface ExtensionContributor {
    val koinModule: Module
    // TODO: remove me
    val koinScope: ScopeID
}
