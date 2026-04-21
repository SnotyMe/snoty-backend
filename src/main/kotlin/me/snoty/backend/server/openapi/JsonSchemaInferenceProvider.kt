package me.snoty.backend.server.openapi

import io.ktor.openapi.*
import org.koin.core.annotation.Single

@Single
fun provideJsonSchemaInference(): JsonSchemaInference = KotlinxJsonSchemaInference
