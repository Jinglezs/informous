import org.jetbrains.kotlin.cli.jvm.main

plugins {
    kotlin("jvm") version "1.8.10"
}

group = "com.knockturnmc"
version = "1.0"

repositories {
    google()
    mavenCentral()

    maven {
        name = "Sonatype Snapshots"
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        url = uri("https://repo.knockturnmc.com/content/repositories/knockturn-public/")
    }
}

dependencies {
    compileOnly("dev.lynxplay.ktp:ktp-api:1.19.3-R0.1-SNAPSHOT")
    implementation("dev.kord:kord-core:0.8.0-M17")
    implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.5.6-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}

kotlin {
    jvmToolchain(17)
}

// Copy runtime dependencies into final jar
tasks.jar {
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)

    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}