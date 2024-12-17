plugins {
    kotlin("jvm") version "2.1.0"
//    application
}

repositories.mavenCentral()

dependencies {
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}

kotlin {
    jvmToolchain(21)
}

//application.mainClass.set("day16.Day16Kt")
