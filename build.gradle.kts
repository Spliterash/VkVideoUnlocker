plugins {
//    `java-library`
    kotlin("jvm") version "1.9.23"
    kotlin("kapt") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.minimal.application") version "4.3.4"
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
    sourceCompatibility = JavaVersion.toVersion("17")
}
tasks {
    compileKotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
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
}