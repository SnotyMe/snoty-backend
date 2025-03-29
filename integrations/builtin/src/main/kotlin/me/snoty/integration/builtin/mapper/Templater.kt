package me.snoty.integration.builtin.mapper

import org.bson.Document
import org.slf4j.Logger

typealias Templater = (logger: Logger, settings: MapperSettings, data: Document) -> Document
