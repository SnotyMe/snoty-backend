package me.snoty.integration.common.wiring.node.impl.mapper

import org.bson.Document

typealias Templater = (settings: MapperSettings, data: Document) -> Document
