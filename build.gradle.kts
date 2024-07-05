import java.text.SimpleDateFormat
import java.util.*

plugins {
    kotlin("jvm") version "2.0.0"
}

val lowercaseName = project.name.lowercase(Locale.getDefault())
group = "io.github.yin.$lowercaseName"
version = ""; val pluginVersion = SimpleDateFormat("yyyy.MM.dd").format(Date()) + "-SNAPSHOT"
val pluginAuthor = "尹"
val pluginLibraries = listOf("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

//    maven("https://libraries.minecraft.net/")
//    maven("https://repo.codemc.io/repository/nms/")
}

val minecraftVersion = "1.21"
dependencies {
    compileOnly("net.md-5:bungeecord-api:$minecraftVersion-R0.1-SNAPSHOT")

//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

tasks.named<ProcessResources>("processResources") {
    filesMatching("bungee.yml") {
        expand(
            mapOf(
                "project" to project,
                "pluginVersion" to pluginVersion,
                "pluginAuthor" to pluginAuthor,
                "pluginLibraries" to pluginLibraries.joinToString("") { "\n  - \"$it\"" }
            )
        )
    }
}

kotlin {
    jvmToolchain(21)
}