plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.8.0"
}

group = "ru.spliterash"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
tasks.jar {
    manifest {
        attributes["Main-Class"] = "ru.spliterash.vkVideoUnlocker.VkVideoUnlockerMainKt"
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
    implementation("com.vk.api:sdk:1.0.14")

    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
}

tasks.assemble { dependsOn(tasks.shadowJar) }