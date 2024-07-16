package me.snoty.integration.builtin.mapper

import org.bson.Document

typealias Templater = (settings: MapperSettings, data: Document) -> Document
