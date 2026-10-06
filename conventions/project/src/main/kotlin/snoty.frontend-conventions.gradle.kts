import com.github.gradle.node.pnpm.task.PnpmTask
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

plugins {
    kotlin("jvm")
    id("com.github.node-gradle.node")
}

val frontendDir = layout.projectDirectory.dir("frontend")

node {
    version = "24.21.0"
    download = true
    distBaseUrl = null

    nodeProjectDir = frontendDir

    val packageJsonFile = frontendDir.file("package.json").asFile
    val packageContent = Json.decodeFromString<JsonObject>(packageJsonFile.readText())
    pnpmVersion = packageContent["packageManager"]!!.jsonPrimitive.content.substringAfter("@")
}

val frontendGenerate = tasks.register("frontendGenerate", PnpmTask::class) {
    args = listOf("run", "generate")
    environment = mapOf(
        "FRONTEND_STATIC_FILE_HOSTING" to "true",
    )
    dependsOn(tasks.pnpmInstall)
    inputs.dir(frontendDir.dir("app"))
    inputs.file(frontendDir.file("nuxt.config.ts"))
    inputs.file(frontendDir.file("package.json"))
    outputs.dir(frontendDir.dir(".output/public"))
}

val frontendCopy = tasks.register("frontendCopy", Copy::class) {
    from(frontendDir.dir(".output/public"))
    into(layout.buildDirectory.dir("resources/main/frontend"))
    dependsOn(frontendGenerate)
}

tasks.processResources {
    if (project.findProperty("frontend.enabled") == "false" || System.getProperty("frontend.enabled") == "false") {
        return@processResources
    }

    dependsOn(frontendCopy)
}
