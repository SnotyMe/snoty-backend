package me.snoty.node.mapper

import org.bson.Document
import org.koin.core.component.KoinComponent
import org.slf4j.Logger

typealias Templater = KoinComponent.(logger: Logger, settings: MapperSettings, data: Document) -> Document
