package me.snoty.extension

import org.koin.core.module.Module

interface ExtensionContributor {
    val koinModule: Module
}
