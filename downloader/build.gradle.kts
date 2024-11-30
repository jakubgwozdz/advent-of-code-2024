plugins {
    kotlin("jvm") version "2.1.0"
    application
}

repositories.mavenCentral()

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("ch.qos.logback:logback-classic:1.5.11")
    implementation("com.slack.api:slack-api-client:1.44.2")
    implementation("com.slack.api:slack-api-client-kotlin-extension:1.44.2")
}

kotlin {
    jvmToolchain(21)
}

application.mainClass.set("MainKt")
