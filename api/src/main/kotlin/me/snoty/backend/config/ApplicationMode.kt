package me.snoty.backend.config

enum class ApplicationMode(
    val withFrontend: Boolean,
) {
    FULLSTACK(
        withFrontend = true,
    ),
    BACKEND(
        withFrontend = false,
    ),
}
