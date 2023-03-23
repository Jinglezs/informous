plugins {
    kotlin("jvm") version "1.8.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.knockturnmc"
version = "1.0"

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.knockturnmc.com/content/repositories/knockturn-public/")
}

val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:1.8.10"
val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4"
val kord = "com.kotlindiscord.kord.extensions:kord-extensions:1.5.6-SNAPSHOT"

dependencies {
    compileOnly("dev.lynxplay.ktp:ktp-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly(kotlinReflect)
    compileOnly(kotlinCoroutines)
    implementation(kord)
}

apply(plugin = "com.github.johnrengelman.shadow")

kotlin {
    jvmToolchain(17)
}

tasks.processResources {
    expand(
        "version" to project.version,
        "kotlinReflect" to kotlinReflect,
        "kotlinCoroutines" to kotlinCoroutines,
        "kord" to kord
    )
}

tasks.shadowJar {
    archiveClassifier.set("")
    minimize()
}