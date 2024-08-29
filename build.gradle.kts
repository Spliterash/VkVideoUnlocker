plugins {
//    `java-library`
    kotlin("jvm") version "2.0.0"
    kotlin("kapt") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.minimal.application") version "4.3.3"
}

group = "ru.spliterash"
version = "1.0.0"

repositories {
    mavenCentral()
}
application {
    mainClass.set("ru.spliterash.vkVideoUnlocker.application.VkUnlockerApplication")
}
java {
    sourceCompatibility = JavaVersion.toVersion("21")
}
tasks {
    compileKotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
}

micronaut {
    runtime("netty")
}

dependencies {
    // Kotlin stuff
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")
    // Micronaut
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    runtimeOnly("ch.qos.logback:logback-classic")
    // Jackson
    runtimeOnly("io.micronaut:micronaut-jackson-databind")
    // Http Client
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:okhttp-coroutines-jvm:5.0.0-alpha.11")
    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // JDBI
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("io.micronaut.sql:micronaut-jdbi")
    implementation("org.jdbi:jdbi3-kotlin")
    // MariaDB
    implementation("org.mariadb.jdbc:mariadb-java-client")

    // Yaml
    runtimeOnly("org.yaml:snakeyaml")

// JSOUP
    implementation("org.tukaani:xz:1.10")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("org.apache.commons:commons-compress:1.27.1")
    implementation("io.v47.jaffree:jaffree:1.0.0")

}