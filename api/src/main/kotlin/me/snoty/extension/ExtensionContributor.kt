package me.snoty.extension

import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier

interface ExtensionContributor {
    val koinModule: Module
    val koinScope: Qualifier
}
