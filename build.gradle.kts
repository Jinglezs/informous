plugins {
    kotlin("jvm") version "1.8.10"
}

group = "com.knockturnmc"
version = "1.0"

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.knockturnmc.com/content/repositories/knockturn-public/")
}

dependencies {
    compileOnly("dev.lynxplay.ktp:ktp-api:1.19.3-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.5.6-SNAPSHOT")

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